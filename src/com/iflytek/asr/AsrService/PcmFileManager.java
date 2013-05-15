package com.iflytek.asr.AsrService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class PcmFileManager 
{
	public static String sdcardpath = "/sdcard/HaierVoiceCamera/aitalk4_pcm/";

	public static int filenum = 0;

	public static int PCM_BUFFER_SIZE = 1024*5000;
	
	public static String filename = "";

	private static final String TAG = "___PCMFileManager___";
	
	private static boolean DEBUG_MODE = false;
	
	/**
	 * 保存PCM数据的缓冲区
	 */
	private static byte[] pcmBuffer = new byte[PCM_BUFFER_SIZE];
	/**
	 * 缓存数据当前大小
	 */
	private static int    pcmOffset = 0;
	
	/**
	 * 清空缓存数据
	 */
	public static void ClearBuffer()
	{
		pcmOffset = 0;
	}
	
	/**
	 * 将录音数据保存到缓存中
	 * @param buffer
	 * @param length
	 */
	public static void WriteBuffer(byte[] buffer,int length)
	{
		if(DEBUG_MODE)
		{
			java.lang.System.arraycopy(buffer, 0, pcmBuffer, pcmOffset, length);
			pcmOffset += length;
		}
	}
	
	/**
	 * 将pcm数据保存到文件中
	 */
	public static void WriteToFile() 
	{
		if(DEBUG_MODE)
		{
			String filename = getPcmFile();
			try 
			{
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(pcmBuffer, 0, pcmOffset);
				fos.close();
			} catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * 创建一个pcm文件
	 * @return
	 */
	private static String getPcmFile() 
	{
		if(DEBUG_MODE)
		{
			File temp = new File(sdcardpath);
			if (!temp.exists())
				temp.mkdirs();
			filename = sdcardpath + "record_" + filenum + ".pcm";
			temp = new File(filename);
			if (!temp.exists()) {
				filenum = 0;
				filename = sdcardpath + "record_" + filenum + ".pcm";
				temp = new File(filename);
			}
			while (temp.exists()) {
				// Log.d(TAG, "record file exist = " + filename);
				filenum++;
				filename = sdcardpath + "record_" + filenum + ".pcm";
				temp = new File(filename);
			}
			try {
				temp.createNewFile();
				// Log.d(TAG, "record filename = " + filename);
			} catch (IOException e) {
				Log.d(TAG, "record  file create error!" + filename);
			}
			return filename;
		}
		return "";
	}
}