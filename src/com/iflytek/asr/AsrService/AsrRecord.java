package com.iflytek.asr.AsrService;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * ASR AudioRecord manager
 * @author zhangyun
 *
 */
public class AsrRecord {
	private static final String TAG = "AsrRecord";
	private static final int BUFF_SIZE = 64 * 320;           //Receive data buffer size
	private static final int FRAME_BUFF = 16 * 320;          //A frame buffer size
	private static final int SAMPLE_RATE = 16000;             //Sample rate
	private static final int READ_DELAY = 10;                //Read delay time
	private static final int BUFF_IGNORE = 4 * 320;          //Ignore audio data when begin record
	private static AudioRecord mRecord = null;
	private static boolean mCanAppendData = false;
	private static Thread mThreadRecord = null;
	
	
	/**
	 * 鍒涘缓褰曢煶瀵硅薄
	 * @author yjzhao
	 */
	public static int createRecord()
	{
		mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 
			SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, 
			 AudioFormat.ENCODING_PCM_16BIT, BUFF_SIZE);
		if (null == mRecord)
		{
			Log.d(TAG,"AudioRecord is null");
			return -1;
		}
		
		mCanAppendData = false;
		return 0;
	}
	
	/**寮�惎褰曢煶瀵硅薄
	 * @author yjzhao
	 * @return
	 */
	public static int  startRecord()
	{
		if (null == mRecord){
			Log.d(TAG,"AudioRecord is null");
			return -1;
		}
		if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED )
		{
			Log.d(TAG,"startRecord state uninitialized");
			return -1;
		}else
		{
		    mCanAppendData = false;
		    if (null != mThreadRecord)
			{
		    	try
			    {
		    		mThreadRecord.join();
			    }
			    catch (InterruptedException e)
			    {
			    	// TODO: handle exception
			    }
			}
		    Log.d(TAG, "before startRecording......." + System.currentTimeMillis());
		    PcmFileManager.ClearBuffer();
		    mRecord.startRecording();
			Log.d(TAG, "after startRecording......." + System.currentTimeMillis());			
			
			class ThreadRecord implements Runnable
			{			
				public void run() 
				{				
				    	byte [] mBuff = new byte[BUFF_SIZE];									
					
	    				if (mRecord == null){
	    					Log.d(TAG,"ThreadRecord mRecord null ");		
	    					return;
	    				}
	    				if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED)
	    				{
	    					Log.d(TAG, "这里出错啦！");
	    					Log.d(TAG,"ThreadRecord mRecord uninitialized ");		
	    					return;
	    				}
	    				if( AudioRecord.RECORDSTATE_STOPPED == mRecord.getRecordingState())
	    				{
	    					Log.d(TAG, "这里出错啦！");
	    					Log.d(TAG,"ThreadRecord mRecord stopped");		
	    					return;
	    				}
					// modified by yjzhao @ 2010.8.12
					mRecord.read(mBuff,0,BUFF_IGNORE );
					Log.d(TAG," ignore audio data ...");
						
					RecordFiler.ClearBuffer();
					Log.d("WCDLog", "thread start" + System.currentTimeMillis());
					while (mCanAppendData)
					{
	        				if (mRecord == null){
	        					Log.d(TAG,"ThreadRecord mRecord null ");		
	        					break;
	        				}
	        				if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED
	        					|| AudioRecord.RECORDSTATE_STOPPED == mRecord.getRecordingState()){
	        					Log.d(TAG,"ThreadRecord mRecord uninitialized or stopped");		
	        					break;
	        				}
	        				Log.d(TAG,"ThreadRecord begin read.......");	
	        				int ret = 0;
	        				try {
	        					ret = mRecord.read(mBuff,0,FRAME_BUFF);	  
	        				}catch (Exception e){
	        					Log.e(TAG,e.toString());
	        				}
	        						
	        				if (ret > 0)
	        				{					
	        					// 褰曢煶鏁版嵁鍐欏埌缂撳瓨
	        					PcmFileManager.WriteBuffer(mBuff, ret);
	        					RecordFiler.WriteBuffer(mBuff, ret);
	        					// 褰曢煶鏁版嵁缂撳瓨缁撴潫
	        					if(mCanAppendData)
	        						ret = Asr.appendData(mBuff,ret);
	        					
	        					if (0 != ret){
	        					    	Log.e(TAG, "ThreadRecord append data to ASR error!");
	        						break;
	        					}
	        				}
	        				else
	        				{
	        					Log.e(TAG, "ThreadRecord read data error!");
	        					break;
	        				}
	    					try {
	        					Thread.sleep(READ_DELAY);
	        				} catch (InterruptedException e) {
	        				    	Log.d(TAG,e.toString());		
	        					break;
	        				}
					}
					RecordFiler.WriteToFile();
				}
			
			};
			mThreadRecord = (new Thread(new ThreadRecord()));
		}
				
		return 0;
	}
	
	/**寮�褰曢煶锛岀敱璇嗗埆寮曟搸杩涜閫氱煡
	 * @author yjzhao
	 * @return
	 */
	public static int setCanAppendData() {
		if (null == mRecord){
			Log.d(TAG,"AudioRecord is null");
			return -1;
		}
		if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED ){
			Log.d(TAG,"startRecord state uninitialized");
			return -1;
		}else{
		    	mCanAppendData = true;
		    	mThreadRecord.start();
		}
	    	return 0;
	}

	/**
	 * 鏆傚仠褰曢煶
	 * @author yjzhao
	 */
	public static void stopRecord() {
	    	Log.d("WCDLog","AsrRecord.stopRecord()......" + System.currentTimeMillis());
		if (null != mRecord && (mRecord.getState() == AudioRecord.STATE_INITIALIZED
				|| AudioRecord.RECORDSTATE_RECORDING == mRecord.getRecordingState())){
			Log.d(TAG,"stopRecord ");		
			try {
					PcmFileManager.WriteToFile();
			    	mCanAppendData = false;
			    	mRecord.stop();
			}catch (Exception e){
				Log.d(TAG,e.toString());
			}
		}else{
			Log.d(TAG,"stopRecord  error state ");
		}
	}	
	
	/**閲婃斁褰曢煶瀵硅薄
	 * author yjzhao
	 * @return
	 */
	public static int releaseRecord() 
	{
	    mCanAppendData = false;
	    if (null != mThreadRecord)
		{
	    	try
		    {
	    	    mThreadRecord.join();
		    }
		    catch (InterruptedException e)
		    {
		    	// TODO: handle exception
		    }
		}
		if (null != mRecord && (mRecord.getState() == AudioRecord.STATE_INITIALIZED
			|| AudioRecord.RECORDSTATE_RECORDING == mRecord.getRecordingState()))
		{
		    Log.d(TAG,"stopRecord ");		
		    try {
			mRecord.stop();
			mRecord.release();
			mRecord = null;
		    }
		    catch (Exception e)
		    {
			Log.d(TAG,e.toString());
		    }
		}
		else
		{
		    Log.d(TAG,"stopRecord  error state ");
		}
	    	return 0;
	}
}


