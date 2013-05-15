package com.iflytek.client.recorder;

import com.iflytek.util.log.Logging;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * PCM Recoder
 * 增加录音时长统计 2011-11-17
 * @author iflytek
 *
 */
public class PcmRecorder {
    private static final String TAG = "SPEECH_PcmRecorder";
    
	private static final int DEFAULT_SAMPLE_RATE = 16 * 1000;
//    private static final int DEFAULT_SAMPLE_RATE = 8 * 1000;		// 蓝牙录音的采样率为8K，讯飞语点本地识别引擎不支持
    
	private static final short DEFAULT_BIT_SAMPLES = 16;
	private static final int RECORD_BUFFER_TIMES_FOR_FRAME = 10;
	public static final int DEFAULT_TIMER_INTERVAL = 40; 
	private static final short DEFAULT_CHANNELS = 1;	
	
	private byte[] _dataBuffer = null; 
	private AudioRecord _mRecorder = null;
	private PcmRecordListener _recordListener = null;
	private int framePeriod = 0;
	private Object mReadLock  = new Object();
	private long mStartTime = 0;
	private boolean mIsRecording = false; 
	
	/**
	 * 为了支持通过录音方式自动化测试,增加该参数
	 */
	private static RandomAccessFile mTestRecordFile = null;  
	public PcmRecorder() throws Exception {
		this(DEFAULT_CHANNELS, DEFAULT_BIT_SAMPLES, DEFAULT_SAMPLE_RATE, DEFAULT_TIMER_INTERVAL);
	}
	
	public PcmRecorder(short channels, short bitSamples, int sampleRate, int timeInterval) throws Exception {
	    if (timeInterval % DEFAULT_TIMER_INTERVAL != 0) {
            Logging.e(TAG, "parameter error, timeInterval must be multiple of "
                    + DEFAULT_TIMER_INTERVAL);
            throw new Exception();
        }
	    
	    framePeriod = sampleRate * timeInterval / 1000;
		int recordBufferSize = framePeriod * RECORD_BUFFER_TIMES_FOR_FRAME * bitSamples * channels / 8;
		int channelConfig = (channels == 1 ? AudioFormat.CHANNEL_CONFIGURATION_MONO : AudioFormat.CHANNEL_CONFIGURATION_STEREO);
		int audioFormat = (bitSamples == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);
		
		int min = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		if (recordBufferSize < min) {
			recordBufferSize = min;
			Logging.w("PCM recorder", "Increasing buffer size to " + Integer.toString(recordBufferSize));
		}
		
		_mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
				channelConfig, audioFormat, recordBufferSize);
		
		if(_mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
	        _mRecorder.release();
	        _mRecorder = null;
			Logging.e(TAG, "create AudioRecord error");
			throw new Exception("create AudioRecord error");
		}
		
		_mRecorder.setRecordPositionUpdateListener(_mRecordListener);
		_mRecorder.setPositionNotificationPeriod(framePeriod);
		
		_dataBuffer = new byte[framePeriod * channels * bitSamples / 8];
	}
	 
	/**
	 * 设置测试的录音文件路径
	 * @param filepath
	 */
	public static void setTestRecordFile(String filepath){
	    if (null != mTestRecordFile){
            try {
                mTestRecordFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	    
	    try {	       
            mTestRecordFile = new RandomAccessFile(filepath,"r");
            Logging.d(TAG,"setTestRecordFile " + filepath);
        } catch (FileNotFoundException e) {
            mTestRecordFile = null;
            e.printStackTrace();
        }
	    
	}
	private AudioRecord.OnRecordPositionUpdateListener _mRecordListener = new OnRecordPositionUpdateListener() {
		public void onMarkerReached(AudioRecord recorder) {
		
		}
		public void onPeriodicNotification(AudioRecord recorder) {
            if (null != _mRecorder){ 
                if (mIsRecording){
                    //Logging.d(TAG, "readRecordData into");
                    synchronized (mReadLock) {
                        readRecordData();
                    }
                    //Logging.d(TAG, "readRecordData end");
                }else {
                    Logging.d(TAG, "readRecordData mIsRecording false");
                } 
            }
		}
	};
	
	private int readRecordData() {
		int count = 0;
		try {
			if (_mRecorder != null) {
				if (_mRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
					Logging.d(TAG, "readRecordData END RECORDSTATE_STOPPED");
					return 0;
				}
				count = _mRecorder.read(_dataBuffer, 0, _dataBuffer.length);
				if (count > 0 && _recordListener != null) {
				    if (null != mTestRecordFile){
				        _dataBuffer = readTestRecordFile(_dataBuffer.length);
				    }
					_recordListener.onRecordData(_dataBuffer, count,System.currentTimeMillis() - mStartTime);
				} else {
					Logging.d(TAG, "count = " + count);
				}
			}else{
			    Logging.d(TAG, "readRecordData null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
	}
	
	/**
	 * 从测试文件中读取录音数据
	 * @param length
	 * @return
	 */
	private byte[] readTestRecordFile(int length) {
	    byte [] buffer = new byte[length];
	    int ret = 0;
	    try {
           ret = mTestRecordFile.read(buffer); 
           if (ret < 0){
               buffer[0] = 1; //防止数据当作异常静音数据丢弃
           }
        } catch (IOException e) {
            
        }
	    Logging.d(TAG,"readTestRecordFile ret=" + ret);
        return buffer;
    }

    public void setRecordListener(PcmRecordListener listener) {
		_recordListener = listener;
	}
	
	public void removeRecordListener() {
		_recordListener = null;
	}
	
	public void startRecording() {
	    Logging.d(TAG,"startRecording begin_"); 
	     
	    if (_mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
	        Logging.e(TAG,"startRecording RECORDSTATE_RECORDING"); 
	        return;
	    }
//	    if (BloothHeadsetManager.getInstance() != null) {
//		    BloothHeadsetManager.getInstance().startRecognize();
//	    }
	    mIsRecording = true;
		_mRecorder.startRecording();
		int total = 0;
		mStartTime = System.currentTimeMillis();
		
		while (total < framePeriod * 2) {
		    Logging.d(TAG,"startRecording read begin  total=" + total + " frame=" + framePeriod );
			int count = readRecordData();
			Logging.d(TAG,"startRecording read end=" + count);
			if (count > 0) {
				total += count;
			}else{
			    break;
			}
			
			try {
				Thread.sleep(50);
				Logging.d(TAG,"startRecording sleep");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			
		}
		
		Logging.d(TAG,"startRecording end");
	}
	
	public void stopRecording() { 
	    
		if (_mRecorder != null) {
		    Logging.d(TAG, "stopRecording into");      
		    // FIXME 等读取完成后再Stop 2012-8-13 ，解决部分手机Stop后读取数据阻塞问题
		    mIsRecording = false;
		    if (_mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
		        synchronized (mReadLock) {
	                _mRecorder.stop();
	            } 
		    } 
		    
		    Logging.d(TAG, "stopRecording end");      
//		    if (BloothHeadsetManager.getInstance() != null) {
//			    BloothHeadsetManager.getInstance().stopRecognize();
//		    }
		}
	}
	
	public void release() { 
        if (null != _mRecorder && 
            _mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            stopRecording();  
        }
        // FIXME 在部分机器上release后 read方法会阻塞,增加
        Logging.d(TAG, "release begin"); 
        synchronized (mReadLock) {
            if (_mRecorder != null) {
                _mRecorder.release();
                _mRecorder = null;
            }
            Logging.d(TAG, "release ok");
        }
        
        
        if (null != mTestRecordFile){
            try {
                mTestRecordFile.close();
            } catch (IOException e) { 
            }
            mTestRecordFile = null;
        }
		Logging.d(TAG, "release end");
	}
	
	public int getSampleRate(){
	    if (_mRecorder != null){
	        return _mRecorder.getSampleRate();
	    }else{
	        return DEFAULT_SAMPLE_RATE;
	    }
	}
	
	public boolean isRecording(){
        if (_mRecorder != null){
            return _mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
        }else{
            return false;
        }
    }
}
