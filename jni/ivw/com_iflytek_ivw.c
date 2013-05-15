/***
*  Yunzhang 2012-2-28 
* For viafly3.0 speech awaken modul.
*
* 2013-1-23 IVW Version2.0
*/

#include <stdio.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <jni.h>
#include <android/log.h>
#include "ivIVW.h"

// Debug veriosn,save log and open logcat
#define DEBUG_LOGCAT


#ifdef DEBUG_LOGCAT
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "SPEECH_Ivw_jni", __VA_ARGS__))

#else
#define LOGD(...)  
#endif 

#define AITALK_SDK_SN "For iFlyTek Developer#S2W7OHBEJZYYHMCRX8GR5KVO2VDKHVMZOPOJWSRK777RC4YH"
#define IVW_OBJ_SZIE 10 * 1024
#define IVW_RAM_SZIE 38
#define MIN_DATA_SIZE 160


static struct java_asr_class {
	JavaVM *vm;
	JNIEnv *env;				
	jclass clazzTts;
	jmethodID onJniResult;
	jmethodID onJniMessage;
} m_JavaAsrClass;
 



static ivPointer m_pIvwObj = NULL;
static ivPointer m_pResidentRAM = NULL;
static char * m_pResData = NULL;
static ivInt16  m_nCM = 20; 

static void JniInit(JNIEnv * env)
{
	jclass classTmp =  (*env)->FindClass(env, "com/iflytek/aitalk/Ivw");

	if (NULL == classTmp){
		LOGD("FindClass is null");
		return;
	}

	m_JavaAsrClass.clazzTts = (jclass)(*env)->NewGlobalRef(env, classTmp);
	m_JavaAsrClass.env = env;

	LOGD("JniInit  into");
	if (NULL != m_JavaAsrClass.clazzTts ){
		m_JavaAsrClass.onJniMessage  = (*env)->GetStaticMethodID(env, m_JavaAsrClass.clazzTts
			,"onCallMessage","(I)I");
		m_JavaAsrClass.onJniResult  = (*env)->GetStaticMethodID(env, m_JavaAsrClass.clazzTts
			,"onCallResult","()I");
		
		LOGD("JniInit  ok");
	}
}

static void JniDinit()
{
	if (NULL != m_JavaAsrClass.clazzTts){
		(*m_JavaAsrClass.env)->DeleteGlobalRef(m_JavaAsrClass.env, m_JavaAsrClass.clazzTts);
		m_JavaAsrClass.clazzTts = NULL;
	}	
	m_JavaAsrClass.env = NULL;
	m_JavaAsrClass.onJniMessage = 0;
	m_JavaAsrClass.onJniResult = 0;	
	LOGD("JniDInit  ok");
}


static void OnMsgResult()
{
	LOGD("OnMsgResult");
	if (m_JavaAsrClass.env && m_JavaAsrClass.clazzTts
		&& m_JavaAsrClass.onJniResult){
	}else{
		LOGD("OnMsgResult  but java method null");
		return ;
	}
	(*m_JavaAsrClass.env)->CallStaticIntMethod(m_JavaAsrClass.env, m_JavaAsrClass.clazzTts
		,m_JavaAsrClass.onJniResult,(jint)0);
}

static int IVW_Destroy()
{
	int ret = 0;

	if (NULL != m_pIvwObj){
		free(m_pIvwObj);
		m_pIvwObj = NULL;		
	}
	if (NULL != m_pResidentRAM){
		free (m_pResidentRAM);
		m_pResidentRAM = NULL;
	}

	if (NULL != m_pResData){
		free(m_pResData);
		m_pResData = NULL;		
	}
	LOGD("IVW_Destroy Ok");
	return ret;
}


jint JNICALL nativeCreate(JNIEnv *env, jobject thiz, jbyteArray res_data,jint size)
{
	int iStatus = 0;
	if (NULL != m_pIvwObj) {
		IVW_Destroy();
	}

	JniInit(env);
	ivSize nObjSize = IVW_OBJ_SZIE;
	ivUInt16 nRamSize = IVW_RAM_SZIE;
	
	m_pIvwObj = (ivPointer )malloc(nObjSize);
	m_pResidentRAM = (ivPointer )malloc(nRamSize);
	
	jbyte* pBuff = (*env)->GetByteArrayElements(env, res_data, NULL);
	m_pResData = (char *) malloc (size);
	memset(m_pResData,0,size);
	LOGD("ResMemCpy res size=%d, copied size= %d", size, memcpy(m_pResData,(char *)pBuff, size));
	(*env)->ReleaseByteArrayElements(env, res_data, pBuff, 0);
	pBuff = NULL;
	
	iStatus = IvwCreate(m_pIvwObj,&nObjSize,m_pResidentRAM ,&nRamSize ,m_pResData);	
	LOGD("IVW_Create size %d ret = %d",size,iStatus);
	
    
	return iStatus;
}

jint JNICALL nativeDestroy(JNIEnv *env, jobject thiz)
{
	JniDinit();
	return  IVW_Destroy();
}


jint  JNICALL nativeAppendData(JNIEnv *env, jobject thiz, jbyteArray dataArray, jint dataSize)
{
	int app_ret = 0;
	int run_ret = 0;
	int pos = 0;
	int submit = 0;
	int i = 0;
	if (dataSize <= 0 ){
		return 0;
	}

	if (NULL == m_pIvwObj){
		LOGD("JniAppendData m_pIvwObj NULL");
		return -1;	
	}

	jbyte* pBuff = (*env)->GetByteArrayElements(env, dataArray, NULL);

	if (NULL == pBuff){
		LOGD("JniAppendData dataArray is null");
		return -1;
	}
	submit = dataSize / MIN_DATA_SIZE ;
	for (i = 0;i < submit; i ++){
		app_ret = IvwAppendAudioData(m_pIvwObj, (ivPInt16) (pBuff + MIN_DATA_SIZE * i), MIN_DATA_SIZE >> 1);	
		run_ret = IvwRunStep(m_pIvwObj,&m_nCM);		
		if (IvwErr_WakeUp == run_ret){
			OnMsgResult();	
			break;
		}
	}
	
	(*env)->ReleaseByteArrayElements(env, dataArray, pBuff, 0); 
	return app_ret;	
}


jint nativeSetParam(JNIEnv *env, jobject thiz, jint iParam, jint iValue)
{
	if (NULL == m_pIvwObj){
			LOGD("nativeSetParam m_pIvwObj NULL");
			return -1;	
	}
	return IvwSetParam(m_pIvwObj,(ivUInt32) iParam,(ivInt16)iValue);
}

jint nativeReset(JNIEnv *env, jobject thiz)
{
	if (NULL == m_pIvwObj){
				LOGD("nativeReset m_pIvwObj NULL");
				return -1;	
	}
	return IvwReset(m_pIvwObj);
}


/*
 * Table of methods associated with a single class.
 */
static JNINativeMethod gMethods[] = {
	//name, signature, funcPtr
    {"JniCreate",          "([BI)I", (void*)nativeCreate},
    {"JniDestroy",         "()I", (void*)nativeDestroy},
    {"JniReset",         "()I", (void*)nativeReset},
    {"JniAppendData",      "([BI)I", (void*)nativeAppendData},
	{"JniSetParam",        "(II)I", (void*)nativeSetParam},

};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 */
static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env,
           "com/iflytek/aitalk/Ivw",
            gMethods, sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    assert(env != NULL);

    if (!registerNatives(env)) {
        goto bail;
    }

	m_JavaAsrClass.vm = vm;
	
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}

