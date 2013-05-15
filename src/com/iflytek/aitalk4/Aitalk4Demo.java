package com.iflytek.aitalk4;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
 
import com.haier.voicecamera.R;
import com.iflytek.asr.RecognitionResult;
import com.iflytek.asr.AsrService.Asr;
import com.iflytek.asr.AsrService.AsrInstance;
import com.iflytek.asr.Recognizer.IRecognitionListener;


import android.app.Activity;
import android.app.AlertDialog; 
import android.content.DialogInterface; 
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log; 
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView; 
import android.widget.Toast;

public class Aitalk4Demo extends Activity implements OnClickListener, IRecognitionListener {
    private static final String TAG = "Asr_Aitalk4Demo";
    
	
	/**
	 * 定义识别消息类型
	 */
	private static final int MSG_BEGIN_OF_SPEECH = 1;

	private static final int MSG_BUFFER_RECEIVERD = 2;

	private static final int MSG_END_OF_SPEECH = 3;

	private static final int MSG_ERROR = 4;

	private static final int MSG_RESULTS = 5;

	private static final int MSG_BEGINNING_OF_RECORD = 6;

	private static final int MSG_END_OF_RECORD = 7;
	
	private ImageView mImgMic;	                    //麦克风图标
	private int mLastErrorId = 0;
	private List<RecognitionResult> mLastResults = null;  //识别结果
	private Button mBtnRetry;

	private boolean mBindOk = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.aitalk_main);        
        mImgMic = (ImageView)this.findViewById(R.id.microphone_view);
        mBtnRetry = (Button)this.findViewById(R.id.btnRetry);
        this.findViewById(R.id.btnRetry).setOnClickListener(this);  
        Asr.init(); //创建ESR
        onAsrInit(); //初始语法
        Asr.setParam(7,1);
	}

	@Override
	public void finish() {
		
		AsrInstance.getInstance().Destory(); //销毁ESR
		super.finish();
	}
 
	
	
	/**
	 * 开始识别
	 */
	private void onButtonRetryClick() {
		Log.d(TAG,"onButtonRetryClick__0__");
		if (mBindOk){
			mBtnRetry.setEnabled(false);	//防止重复按下
			
			AsrInstance.getInstance().setScene("mmenu");
			Asr.setParam(7, 1);
			Asr.setParam(1, 5);
			AsrInstance.getInstance().startListening(this);
		}
	}


	//@Override
	public void onAsrInit() { 
		//构建语法;语法文件存在assets目录中;
		try {
			InputStream is = this.getAssets().open("mmenu.bnf");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			int ret = Asr.buildGrammar(buffer, size);
			if (0 != ret){
				Log.d(TAG,"buildGrammar error return=" + ret);
			}		
				
			buffer = null;
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		Toast.makeText(this,this.getString(R.string.init_ok),Toast.LENGTH_SHORT).show();
		mBindOk  = true;
    }

	/**
	 * 处理各种消息的Handler类	 */
	private Handler mMsgReciver = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_BEGIN_OF_SPEECH:
				break;
			case MSG_BUFFER_RECEIVERD:
				break;
			case MSG_END_OF_SPEECH:				 		
				break;
			case MSG_ERROR:		 
				onErrorMsg();
				mBtnRetry.setEnabled(true);
				break;
			case MSG_RESULTS: 
				onResultsMsg();
				mBtnRetry.setEnabled(true);
				break;
			case MSG_BEGINNING_OF_RECORD:	
				onBeginRecord();			
				break;
			case MSG_END_OF_RECORD:
				onEndRecord();
				mBtnRetry.setEnabled(true);
				break;
			}			
		}
	};
	
	/**
	 * 识别错误
	 */
	private void onErrorMsg() {
		String msg = "识别出错";
		switch (mLastErrorId){		 
		case RecognitionResult.NETWORK_TIMEOUT:// Network operation timed out.
			msg = this.getString(R.string.res_net_timeout);
			break;
		case RecognitionResult.NETWORK_ERROR: // Other network related errors.
			msg = this.getString(R.string.res_net_error);
			break;
		case RecognitionResult.AUDIO_ERROR: // Audio recording error.
			msg = this.getString(R.string.res_recode_error);
			break;
		case RecognitionResult.CLIENT_ERROR:// Other client side errors.
			msg = this.getString(R.string.res_client_error);
			break;			
		case RecognitionResult.SPEECH_TIMEOUT: // No speech input
			msg = this.getString(R.string.res_speech_timeout);
			return;
		case RecognitionResult.NO_MATCH: // No recognition result matched.
			msg = this.getString(R.string.res_no_match);
			break;
		case RecognitionResult.RESPONSE_TIMEOUT: // User response timeout.
			msg = this.getString(R.string.res_response_timeout);
			break;
		}
		
		new AlertDialog.Builder(this) 
        .setTitle(this.getString(R.string.msg_title)) 
        .setMessage(msg + this.getString(R.string.msg_ok_to_repeat)) 
        .setIcon(R.drawable.ico_mic_retry) 
        .setPositiveButton(this.getString(R.string.msg_ok), new DialogInterface.OnClickListener() { 
       public void onClick(DialogInterface dialog, int whichButton) { 
    	   onButtonRetryClick();
        } 
        }) 
        .setNegativeButton(this.getString(R.string.msg_cancle), new DialogInterface.OnClickListener() { 
       public void onClick(DialogInterface dialog, int whichButton) { 
    	   	//取消按钮事件 
        } 
        }) 
        .show(); 	
	}	
	
	/**
	 * 结束录音
	 */
	protected void onEndRecord() {
		mImgMic.setImageResource(R.drawable.ico_mic_off);		
	}
	
	/**
	 * 开始录音
	 */
	protected void onBeginRecord() {
		Toast.makeText(this,this.getString(R.string.msg_begin_record),Toast.LENGTH_LONG).show();
		mImgMic.setImageResource(R.drawable.ico_mic_on);
		
	}

	/**
	 * 识别结果返回
	 */
	private void onResultsMsg() {
		if (mLastResults.size() == 0){
			new AlertDialog.Builder(this) 
	        .setTitle(this.getString(R.string.msg_title)) 
	        .setMessage(this.getString(R.string.res_no_match) +
	        		this.getString(R.string.msg_ok_to_repeat)) 
	        .setIcon(R.drawable.ico_mic_retry) 
	        .setPositiveButton(this.getString(R.string.msg_ok), new DialogInterface.OnClickListener() { 
	       public void onClick(DialogInterface dialog, int whichButton) { 
	    	   onButtonRetryClick();
	        } 
	        }) 
	        .setNegativeButton(this.getString(R.string.msg_cancle), new DialogInterface.OnClickListener() { 
	       public void onClick(DialogInterface dialog, int whichButton) { 
	    	   	//取消按钮事件 
	        } 
	        }) 
	        .show(); 
			return;
		}else{
			//处理识别结果
			
			RecognitionResult result = mLastResults.get(0);
			
			Log.d(TAG,"Confidence" + result.toString());
			int nSlots = result.mSlotList.size();
			String resMsg = "";
			for (int i = 0; i < nSlots; i ++){
				resMsg += result.mSlotList.get(i).mItemTexts[0];
			}
			resMsg += " Confidence=" + result.mConfidence;
			new AlertDialog.Builder(this) 
	        .setTitle(this.getString(R.string.msg_hava_res)) 
	        .setMessage(resMsg) 
	        .setIcon(R.drawable.ico_mic_on) 
	        .setPositiveButton(this.getString(R.string.msg_return), new DialogInterface.OnClickListener() { 
	        	public void onClick(DialogInterface dialog, int whichButton) { 
	    	  
	        } 
	        }) 
	        
	        .show(); 
			
		}			
	}
	 
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnRetry:
			onButtonRetryClick();	//开始识别;
			break;  
		} 
	}
	
	/**
	 * 实现服务的回调接口 IRecognitionListener
	 */
	public void onBeginningOfRecord() {
		Log.d(TAG,"onBeginningOfRecord" );
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BEGINNING_OF_RECORD), 0);
	}

	public void onBeginningOfSpeech() {

		Log.d(TAG,"onBeginningOfSpeech");
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BEGIN_OF_SPEECH), 0);

	}

	public void onBufferReceived(byte[] buffer) {
		Log.d(TAG,"onBufferReceived length=" + buffer.length);
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_BUFFER_RECEIVERD), 0);
	}

	public void onEndOfRecord() {
		Log.d(TAG,"onEndOfRecord" );	
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_END_OF_RECORD), 0);
	}

	public void onEndOfSpeech() {
		Log.d(TAG,"onEndOfSpeech");
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_END_OF_SPEECH), 0);	
	}

	public void onError(int error) {
		mLastErrorId = error;
		Log.d(TAG,"on error=" + error);
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_ERROR), 0);
	}

	public void onResults(List<RecognitionResult> results, long key) {
		Log.d(TAG,"on results =" + results.size());
		mLastResults = results;
		mMsgReciver.sendMessageDelayed(mMsgReciver.obtainMessage(MSG_RESULTS), 0);			

	}
}