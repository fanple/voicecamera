package com.iflytek.asr.AsrService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.iflytek.asr.RecognitionResult;
import com.iflytek.asr.Recognizer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Asr 
{ 
	private static final String TAG = "AITALK_Asr";
	public static final String DIR_RESOURCE = "/sdcard/HaierVoiceCamera/";
	public static String DIR_GRAMMAR = "/sdcard/HaierVoiceCamera/";
	
	//Message define
	public static final int MSG_START_RECORD      = 0x310;
	public static final int MSG_STOP_RECORD       = 0x311;
	
	public static final int MSG_SPEECH_START      = 0x401;
	public static final int MSG_SPEECH_END        = 0x402;
	public static final int MSG_SPEECH_FLUSH_END  = 0x403;
	public static final int MSG_SPEECH_NO_DETECT  = 0x40f; 
	
	public static final int MSG_RESPONSE_TIMEOUT  = 0x410;
	public static final int MSG_SPEECH_TIMEOUT    = 0x411;
	public static final int MSG_END_BY_USER       = 0x412;
	
	public static final int MSG_HAVE_RESULT       = 0x500;	
	
	
	public static final int MSG_DIALOG_CLOSE      = 0x901;
		
	//Parameter type define
	
	public static final int PARAM_SENSITIVITY = 1;	
	public static final int PARAM_RESPONSETIMEOUT = 2;
	public static final int PARAM_SPEECHTIMEOUT = 3;
	public static final int PARAM_SPEECHNOTIFY = 4;
	public static final int PARAM_AUDIODISCARD = 5;
	public static final int PARAM_ENHANCEVAD = 6;
	public static final int PARAM_DISABLEVAD = 7;
	
	
	//Thread wait time
	private static final int TIMEOUT_WAIT_QUEUE = 2000;

	//Thread lock
	private static final ReentrantLock asrRunLock = new ReentrantLock();
	//for call back
	private static Recognizer.IRecognitionListener mCallback = null;
	private static final List<RecognitionResult> mResult = new ArrayList<RecognitionResult>();

	/**
	 * When receive message process
	 * onStartRecord,onResponseTimeout,onSpeechTimeout,onFinish,
	 * 
	 */
	private static Handler mMsgHandler = new Handler() { 
		@Override
		public void handleMessage(Message msg)
		{ 
			switch (msg.what)   
			{			
			case MSG_START_RECORD:
				Asr.startRecordCallback();
				if (AsrRecord.setCanAppendData() < 0){
					Asr.errorCallback( RecognitionResult.AUDIO_ERROR);	
				}
				break;
			case MSG_STOP_RECORD:
				AsrRecord.stopRecord();
				Asr.endRecordCallback();
				break;
			case MSG_SPEECH_START:
				Log.d(TAG,"MSG_SPEECH_START");
				speechStartCallback();
				break;
			case MSG_SPEECH_END:
				Log.d(TAG,"MSG_SPEECH_END");
				speechEndCallback();
				break;
			case MSG_SPEECH_FLUSH_END:
				Log.d(TAG,"MSG_SPEECH_FLUSH_END");
				break;				
			case MSG_SPEECH_NO_DETECT:
				Log.d(TAG,"MSG_SPEECH_NO_DETECT");
				break;
			case MSG_RESPONSE_TIMEOUT:
				Log.d(TAG,"MSG_RESPONSE_TIMEOUT");
				errorCallback( RecognitionResult.RESPONSE_TIMEOUT);
				break;
			case MSG_SPEECH_TIMEOUT:
				Log.d(TAG,"MSG_SPEECH_TIMEOUT");
				errorCallback( RecognitionResult.SPEECH_TIMEOUT);
				break;
			case MSG_END_BY_USER:
				Log.d(TAG,"MSG_END_BY_USER");
				break;	
			case Asr.MSG_HAVE_RESULT:		
				Log.d(TAG,"MSG_HAVE_RESULT FROM MSGHANDER");
				Asr.resultCallback();
				break;
			default:
				Log.d(TAG,"unkown  message: " + msg.what);
				break;
			}
		} 
	}; 
    
	/**
	 * 只开一个高优先级的线程；不开多线程；仅独占式执行AsrRun
	 * @param item
	 */
	private static void startRunThread()
	{
		class AsrRunThread implements Runnable{
			boolean isAsrRunable = false;
			int nRet = 0;
			public void run() 
			{ 
				mResult.clear();  
				try{
					Log.i(TAG, "AsrRunThread to start");
					//1. lock run
					isAsrRunable = asrRunLock.tryLock(TIMEOUT_WAIT_QUEUE, TimeUnit.MILLISECONDS);
					if (!isAsrRunable){
						Log.e(TAG, "AsrRunThread tryLock  is unavailable");
						Asr.errorCallback( RecognitionResult.ASR_BUSY);
						Thread.sleep(100);
						return ;
					}				
					nRet = JniRunTask();
					if (nRet != 0){
						Log.i(TAG, "AsrRunThread Start Error!");	
						Asr.errorCallback( RecognitionResult.ASR_ERROR);
					}
					Log.i(TAG, "AsrRunThread run ok ");		
					
				}catch (InterruptedException e){
					Log.e(TAG, "AsrRunThread interrupted");
			        e.printStackTrace();
				}finally{
					if (isAsrRunable){
						asrRunLock.unlock();
					}
					Log.i(TAG, "AsrRunThread run End!");
				}			
			}
			
		}
		
		Thread asrRun = (new Thread(new AsrRunThread()));
		asrRun.setPriority(Thread.MAX_PRIORITY);
		asrRun.start();
	}

	
	public static void init()
	{
		Log.d(TAG,"JniCreate");
		AsrRecord.createRecord();
		int ret = JniCreate(DIR_RESOURCE,DIR_GRAMMAR);
		Log.d(TAG,"ASR Create = " + ret);
	}
	
	/*
	 * 释放识别资源
	 * @authorL:yjzhao
	 */
	public static void Destory()
	{
	    AsrRecord.releaseRecord();
		stop();
		JniDestroy();
		Log.d(TAG," ASR engine destoryed!");
	}
	
	
	public static void stop() 
	{
		Log.d(TAG," ASR is locked = " + asrRunLock.isLocked());
		if(asrRunLock.isLocked())
		{
			mCallback = null;
			Log.d(TAG," ASR stop begin!");
			JniStop();
			Log.d(TAG," ASR stop end!");
		}
	}
	
	public static void exitService() 
	{
		Log.d(TAG," ASR is locked = " + asrRunLock.isLocked());
		if(asrRunLock.isLocked())
		{
			Log.d(TAG," ASR exit begin!");
			JniExit();
			Log.d(TAG," ASR exit end!");
		}
	}
	
	/**
	 *  始终只能有一个可以运行，先关闭之前的
	 * @param recognizerIntent
	 * @param listener
	 * @return
	 */
	public static int start()
	{
		Log.d("___AitalkProcess__", "____________start time" +System.currentTimeMillis());
		startRunThread();			
		return 0;
	}
	
	
	/**
	 * 设置消息回调类
	 * @param listener
	 */
	public static void setListener(Recognizer.IRecognitionListener listener)
	{
		mCallback = listener;
	}
	
	
	/**
	 * Set scene name
	 * @param sceneName
	 * @return
	 */
	public static int setScene(String sceneName)
	{ 
		return JniStart(sceneName);		
	}
	
	/**
	 * For C call java function
	 */	
	public static int onCallMessage(int msgType)
	{
		Message s_msg = mMsgHandler.obtainMessage(msgType);
		mMsgHandler.sendMessageDelayed(s_msg, 0);		
		return 0;
		
	}	
	/**
	 * For c call java have result
	 * @return
	 */
	public static int onCallResult()
	{		
		int iResCount = 0;
		int iSlotCount = 0;
		int iItemCount = 0;
		//1. get result
		iResCount = JniGetResCount();
		for (int iRes = 0 ;iRes < iResCount;iRes ++)
		{
			//1.1  Get result count,有多少种指令
			int sentenceId = JniGetSentenceId(iRes);
			iSlotCount = JniGetSlotNumber(iRes);
			int confidence = JniGetConfidence(iRes);			
			Log.d(TAG, "onCallResult res:"+ (iRes + 1) + " sentenceId:" + sentenceId 
					+ "  confidence:" + confidence + " SlotCount:"+ iSlotCount );
			RecognitionResult rs = new RecognitionResult(sentenceId,confidence,iSlotCount);
			
			//1.2 Get slot ，有多少条指令
			for (int iSlot = 0;iSlot < iSlotCount;iSlot ++)
			{
				iItemCount = JniGetItemNumber(iRes,iSlot);
				if (iItemCount <= 0 )
				{
					Log.e(TAG,"Error iItemCount < 0");
					continue;
				}
				int itemIds [] = new int[iItemCount];
				String itemTexts [] = new String[iItemCount];
				
				Log.d(TAG,"onCallResult slot:"+ (iSlot + 1) + " iItemCount:" + iItemCount);
				for (int iItem = 0; iItem < iItemCount;iItem ++ )
				{
					itemIds[iItem] = JniGetItemId(iRes,iSlot,iItem);
					itemTexts[iItem] = JniGetItemText(iRes,iSlot,iItem) ;					
					if (null == itemTexts[iItem])
					{
						itemTexts[iItem] = ""; 
					}					
					Log.d(TAG,"onCallResult slot item:"+ (iItem + 1) + " itemTexts:"
							+ itemTexts[iItem] +" itemIds " + itemIds[iItem]);
				}	
				rs.AddSlot(iItemCount, itemIds, itemTexts);
			}
			mResult.add(rs);	
		}
		
		exitService();
		
		//2. Call back to application
		resultCallback();
		Log.d(TAG,"MSG_HAVE_RESULT");
		return 0;
	}
	
	/**
	 * callback application
	 * @param errorId
	 */
	public static void errorCallback(int errorId){
		
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) 
	    {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
		exitService(); 	 
	    try 
	    {
	      cb.onError(errorId);
	    } catch (Exception e) 
	    {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }
	    Log.v(TAG, "IRecognitionListener : hava error");			
	}
	
	public static void speechStartCallback(){
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
	 	 
	    try {
	      cb.onBeginningOfSpeech();
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }	
	}
	
	public static void speechEndCallback(){
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
	 	 
	    try {
	      cb.onEndOfSpeech();
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }	
	}
	
	public static void startRecordCallback(){
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
	 	 
	    try 
	    {
	    	cb.onBeginningOfRecord();
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }	
	}
	
	public static void endRecordCallback(){
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
	 	 
	    try {
	      cb.onEndOfRecord();
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }	
	}
	
	public static void bufferReceivedCallback(byte [] buffer){
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return;
	    }
	 	 
	    try {
	      cb.onBufferReceived(buffer);
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }	
	}
	
	
	public static void resultCallback()
	{
		Recognizer.IRecognitionListener  cb = mCallback;
	    if (cb == null) {
	    	Log.v(TAG,"IRecognitionListener cb is null");
	      return; 
	    }
	 	  
	    try {
	      cb.onResults(mResult, 0);
	    } catch (Exception e) {
	      // The RemoteCallbackList will take care of removing
	      // the dead object for us.
	    }
	    Log.v(TAG, "IRecognitionListener : have result");				
	}	
	
	
	/**
	 * 以下调用要考虑是否能否在识别运行中同时调用; 
	 * 
	 */
	
	public static int  appendData(byte[] buff, int length) {
		int ret = 0;
		bufferReceivedCallback(buff);
		ret = JniAppendData(buff,length);
		
		return ret;
	}


	public static int addLexiconItem(String name,String word, int id) {		
		int ret = 0;
		ret =  JniLexiconInsertItem(name, word, id);
		return ret;
	}

	public static int createLexicon(String lexiconName) {
		int ret = 0;
		ret = JniLexiconCreate(lexiconName);
		return ret;
	}

	public static int updateLexicon(String name) {
		int ret = 0;
		ret = JniLexiconUpdate(name);	
		return ret;
	}

	public static int buildGrammar(byte[] xmlText, int length) {
		int ret = 0;
		ret = JniBuildGrammar(xmlText, length);
		return ret;
	}

	public static List<RecognitionResult> getRecognitionResults(long key) {
		return mResult;
	}

	public static int makeVoiceTag(String lexiconName, String word,
			byte[] pcmData, int dataLength) {
		int ret = 0;
		ret = JniMakeVoiceTag(lexiconName, word,pcmData,dataLength);
		return ret;
	}

	public static int setParam(int paramId, int value) {
		int ret = 0;
		ret = JniSetParam(paramId, value);
		return ret;
	}	
	
	/**
	 * Java native interface code
	 */
	static {
		System.loadLibrary("Aitalk4");
	}
	
	public native static int JniGetVersion();
	
	private native static int JniCreate(String dirRes,String dirGrammar);	
	private native static int JniDestroy();
	private native static int JniStart(String sceneName);
	private native static int JniStop();
	private native static int JniExit();
	private native static int JniRunTask();
	
	
	private native static int JniGetResCount();
	private native static int JniGetSentenceId(int resIndex);
	private native static int JniGetConfidence(int resIndex);
	private native static int JniGetSlotNumber(int resIndex);
	
	private native static int JniGetItemNumber(int resIndex,int slotIndex);
	private native static int JniGetItemId(int resIndex,int slotIndex,int itemIdex);	
	private native static String JniGetItemText(int resIndex,int slotIndex,int itemIdex);	
		
	private native static int JniAppendData(byte []data,int length);
	private native static int JniBuildGrammar(byte[] xmlText,int length);	
	
	private native static int JniLexiconInsertItem(String name,String word,int id);
	private native static int JniLexiconCreate(String name);
	private native static int JniLexiconUpdate(String name);
	private native static int JniLexiconUnload(String name);
	private native static int JniMakeVoiceTag(String name,String item,byte[] data,int dataLen);
	private native static int JniSetParam(int paramId,int paramValue);
}
