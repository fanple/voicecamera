package com.haier.voicecamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
// import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;
import android.os.Environment;
import android.widget.Toast;

import com.iflytek.aitalk.Ivw;
import com.iflytek.client.recorder.PcmRecordListener;
import com.iflytek.client.recorder.PcmRecorder;

public class ivw20 {
	private static final String TAG = "Ivw20WakeUpDemo";
	// 默认的门限值
	private final int THRESHOLD_DEFAULT = 20;
	// 默认的唤醒资源目录
	private final String RESOURCE_DIR_DEFAULT = 
			Environment.getExternalStorageDirectory().getPath().toString()+"/Ivw20Res/";
	// 默认的唤醒资源
	private final String RESOURCE_FILENAME_DEFAULT = 
			RESOURCE_DIR_DEFAULT + "/ivModel_xfyd.irf";

	private final int IVW_CM_THRESHOLD = 101;
	
	private int cntWakeUp=0;
	
	private ivw20 pThisMainActivity=this;
	PcmRecorder ivwRecorder;
	
	VoiceCamera voiceCamera=null;
	
	public ivw20(VoiceCamera vc){
		voiceCamera=vc;
	}
	
    public void beginWaitToWakeUp() {

        // 使用默认资源创建唤醒对象
    	/*
        File file_resourcedir = new File(RESOURCE_DIR_DEFAULT);
        if(!file_resourcedir.exists()) {
        	if(file_resourcedir.mkdir()) {
        		//textview_ShowCurrentResource.setText("唤醒资源目录"+RESOURCE_DIR_DEFAULT+"不存在, 自动创建成功, 请将唤醒资源存在此目录，并重启程序!");
        		Log.e(TAG, "唤醒资源目录"+RESOURCE_DIR_DEFAULT+"不存在, 自动创建成功, 请将唤醒资源存在此目录，并重启程序!");
        	} else {
        		//textview_ShowCurrentResource.setText("唤醒资源目录"+RESOURCE_DIR_DEFAULT+"不存在, 尝试创建失败!");
        		Log.e(TAG, "唤醒资源目录"+RESOURCE_DIR_DEFAULT+"不存在, 尝试创建失败!");
        	}
        } else {
        	File file_resource = new File(RESOURCE_FILENAME_DEFAULT);
        	if(!file_resource.exists()) {
        		//textview_ShowCurrentResource.setText("默认的唤醒资源文件"+RESOURCE_FILENAME_DEFAULT+"不存在, 请手动设置资源!");
        		Log.w(TAG, "默认的唤醒资源文件"+RESOURCE_FILENAME_DEFAULT+"不存在, 请手动设置资源!");
        	} else {
                // TODO: 创建唤醒对象
        		this.ChangeWakeUpResource(RESOURCE_FILENAME_DEFAULT);
        	}
        }*/
    	this.ChangeWakeUpResourceFromAssets();
        
        // 设置默认门限值
        pThisMainActivity.SetWakeUpThreshold(THRESHOLD_DEFAULT);
        
        try {
			ivwRecorder = new PcmRecorder();
			ivwRecorder.setRecordListener(new PcmRecordListener() {
				public void onRecordData(byte[] dataBuffer, int length, long timeMillisecond) {
					// TODO Auto-generated method stub
					int ret=0;
					if((ret=Ivw.JniAppendData(dataBuffer, length)) != 0) {
						Log.e(TAG, "JniAppendData Return Error Code: "+ret+", length"+length);
					} else {
						if(Ivw.getResult()) {
							cntWakeUp++;
							//textview_ShowResult.setText("唤醒结果: 唤醒了，唤醒计数 "+cntWakeUp);
							//Toast.makeText(voiceCamera, "等待你的指令", Toast.LENGTH_LONG).show();
							forceStopIVW20();
							voiceCamera.wakeUpByIVW();
						}
					}
				}
			});
			ivwRecorder.startRecording();
			voiceCamera.setTopbarStatus("请说出\"讯飞语点\"唤醒控制.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    /**
     * 强行停止唤醒引擎
     * @param ResFileName
     * @return
     */
    public void forceStopIVW20(){
    	ivwRecorder.stopRecording();
    	Ivw.reset();
    	Ivw.destory();
    }
    
    private boolean ChangeWakeUpResourceFromAssets() {
   		byte[] res;
		int size=0;
		int ret=0;
		InputStream is;
		try {
			is = voiceCamera.getAssets().open("ivModel_xfyd.irf");
			size = is.available();
			res = new byte[size];
			is.read(res);
			is.close();
			
			ret=Ivw.JniDestroy();
			if((ret=Ivw.JniCreate(res, res.length)) != 0 ) {
				//textview_ShowCurrentResource.setText("ResFileName "+ResFileName+". JniCreate Return Error Code = "+ret);
				Log.e(TAG, "JniCreate Return Error Code = "+ret);
				return false;
			} else {
				//textview_ShowCurrentResource.setText("使用资源"+ResFileName+"创建唤醒对象成功");
				Log.i(TAG, "使用资源  创建唤醒对象成功");
				cntWakeUp=0;
				//textview_ShowResult.setText("唤醒结果:");
			}
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
    }
    
    private boolean ChangeWakeUpResource(String ResFileName) {
   		byte[] res;
		int size=0;
		int ret=0;
		FileInputStream file_in_stream;
		try {
			File file_res=new File(ResFileName);
			if(!file_res.exists()) {
				return false;
			} else {
				file_in_stream = new FileInputStream(file_res);
				size = file_in_stream.available();
				res=new byte[size];
				file_in_stream.read(res);
				file_in_stream.close();
				ret=Ivw.JniDestroy();
				if((ret=Ivw.JniCreate(res, res.length)) != 0 ) {
					//textview_ShowCurrentResource.setText("ResFileName "+ResFileName+". JniCreate Return Error Code = "+ret);
					Log.e(TAG, "ResFileName "+ResFileName+". JniCreate Return Error Code = "+ret);
					return false;
				} else {
					//textview_ShowCurrentResource.setText("使用资源"+ResFileName+"创建唤醒对象成功");
					Log.i(TAG, "使用资源"+ResFileName+"创建唤醒对象成功");
					cntWakeUp=0;
					//textview_ShowResult.setText("唤醒结果:");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
    }
    
    private boolean SetWakeUpThreshold(int Threshold) {
		int ret=0;
		if((ret=Ivw.JniSetParam(IVW_CM_THRESHOLD, Threshold)) == 0 ) {
			// 设置成功后显示提示
			//textview_ShowCurrentThreshold.setText("当前门限: " + Threshold);
			Log.i(TAG, "门限设置为"+Threshold);
			return true;
		} else {
			//textview_ShowCurrentThreshold.setText("未成功设置门限"+Threshold+", JniSetParam Error Code: "+ret);
			Log.d(TAG, "未成功设置门限"+Threshold+", JniSetParam Error Code: "+ret);
			return false;
		}
	}
    /*
    private boolean CopyFileToAnotherDir(String oldFile, String DesDir) throws IOException {
    	File file_oldFile = new File(oldFile);
    	File file_DesFile = new File(DesDir+"/"+file_oldFile.getName().toString());
    	Log.i(TAG, "尝试拷贝文件"+file_oldFile.toString()+"到"+file_DesFile);
    	if(file_DesFile.exists()) {
    		Log.d(TAG, "目标文件已存在, "+"保留原目标文件");
    		return true;
    	} else if(file_oldFile.exists()) {
    		FileInputStream inStream = new FileInputStream(file_oldFile);
    		FileOutputStream outStream = new FileOutputStream(file_DesFile);
    	    int byteread = 0;
    		byte[] buffer = new byte[1024];
            while((byteread=inStream.read(buffer)) != -1) {  
            	outStream.write(buffer, 0, byteread);  
            }
    		inStream.close();
    		outStream.close();
    		return true;
    	} else {
    		return false;
    	}
    }
    */
}
