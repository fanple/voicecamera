package com.iflytek.client.recorder;

/**
 * 用于录音数据回调的接口
 * @author kuncheng
 *
 */
public interface PcmRecordListener
{
	/**
	 * 录音数据回调
	 * @param dataBuffer	录音数据
	 * @param length   录音数据长度
	 */
    void onRecordData(final byte[] dataBuffer, final int length,long timeMillisecond);
}
