LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE    := ivw-jni
LOCAL_SRC_FILES := com_iflytek_ivw.c
LOCAL_LDLIBS += -llog -L$(LOCAL_PATH)/Lib/ -lIvw20  

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_SHARED_LIBRARY)
