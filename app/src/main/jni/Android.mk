LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := obfuscator

LOCAL_C_INCLUDES += ${LOCAL_PATH}/include
LOCAL_SRC_FILES := obfuscator.cpp

LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)
