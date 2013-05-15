package com.iflytek.asr;

import java.util.List;

import com.iflytek.asr.RecognitionResult;

public class Recognizer 
{
	
	/**
	 * Save system settings key
	 */
	public final static String KEY_ENHANCE_VAD = "asr_enhance_vad";
	public final static String KEY_VAD = "asr_vad";
	public final static String KEY_SENSITIVITY_LEVEL = "asr_sensitivity_level";
	public final static String KEY_SPEECH_TIMEOUT = "asr_speech_timeout";
	public final static String KEY_RESPONSE_TIMEOUT = "asr_response_timeout";
	
	
	// public final static  int DEF_SPEECH_TIMEOUT = 5000;      //default 5; 1-20
	// public final static  int DEF_RESPONSE_TIMEOUT = 5000;     //default 5; 1-5
	public final static  int DEF_SENSITIVITY_LEVEL = 0;       //default 0; 0-5
	public final static  boolean DEF_VAD_ON = true;           //default true;
	public final static  boolean DEF_ENHANCE_VAD_ON = true;   //default true;

	
	/**
	 * 识别回调接口
	 * @author yjzhao @2010.8.21
	 *
	 */
	public interface IRecognitionListener
	{
		/**
		 * 识别开始
		 */
		public void onBeginningOfSpeech();
		/**
		 * 录音数据回调
		 */
		public void onBufferReceived(byte[] buffer);
		/**
		 * 识别结束
		 */
		public void onEndOfSpeech();
		/**
		 * 识别错误返回
		 */
		public void onError(int error);
		/**
		 * 识别结果返回
		 */
		public void onResults(List<RecognitionResult> results, long key);
		/**
		 * 录音开始
		 */
		public void onBeginningOfRecord();
		/**
		 * 录音结束
		 */
		public void onEndOfRecord();
	};
	
}
