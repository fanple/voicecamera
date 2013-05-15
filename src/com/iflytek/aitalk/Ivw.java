package com.iflytek.aitalk;

import android.util.Log;
 

/**
 * @author zhangyun
 * @date 2012-2-28
 *
 * @date 2013-1-23
 * 修改版本为2.0,接口发生变化
 */
public class Ivw {    
    private static boolean mHaveResult;

    public static boolean getResult(){
        return mHaveResult;
    }
    public static void reset(){
        mHaveResult = false;
        JniReset();
    }
    
    public static void destory(){
    	JniDestroy();
    }
    /**
     * For C call java function
     */
    public static int onCallMessage(int msgType) {
        Log.d("Ivw","onCallMessage ..");
        return 0;
    }

    
    /**
     * For c call java have result
     * 
     * @return
     */
    public static int onCallResult() {
        Log.d("Ivw","onCallResult ..");
        mHaveResult = true;
        return 0;
    }

    /**
     * Java native interface code
     */
    static {
        System.loadLibrary("ivw-jni");
    }

    public native static int JniCreate(byte[]res,int size);

    public native static int JniDestroy();

    public native static int JniReset();

    public native static int JniAppendData(byte[] data, int length);
    
    public native static int JniSetParam(int paramId, int paramValue);

}
