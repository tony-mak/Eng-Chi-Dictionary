#include "sqlite3.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <unistd.h>
#include <stdio.h>

unsigned char ENCRYPT_MAPPING[] = {40, 155, 82, 9, 211, 93, 192, 230, 69, 75, 187, 219, 17, 183, 254, 105, 32, 62, 72, 67, 20, 180, 103, 90, 232, 236, 165, 251, 43, 52, 143, 121, 156, 167, 196, 18, 148, 104, 178, 152, 44, 247, 66, 5, 241, 124, 226, 173, 38, 45, 139, 132, 115, 15, 122, 228, 74, 209, 234, 171, 46, 166, 4, 149, 50, 240, 144, 117, 131, 150, 185, 95, 68, 203, 8, 112, 61, 6, 47, 86, 216, 175, 224, 205, 59, 191, 217, 197, 243, 85, 13, 89, 51, 54, 164, 204, 220, 31, 134, 231, 176, 94, 136, 114, 14, 233, 129, 130, 60, 123, 98, 135, 7, 57, 88, 119, 25, 102, 107, 246, 133, 3, 35, 80, 227, 255, 194, 138, 53, 99, 147, 63, 23, 71, 218, 182, 235, 137, 190, 174, 27, 120, 39, 42, 186, 118, 100, 223, 172, 199, 206, 248, 195, 29, 238, 162, 170, 48, 222, 37, 159, 101, 19, 1, 160, 252, 184, 163, 28, 198, 212, 0, 76, 146, 177, 111, 97, 237, 200, 10, 242, 12, 16, 244, 110, 22, 181, 225, 140, 34, 125, 70, 113, 151, 213, 36, 158, 49, 142, 210, 239, 208, 127, 179, 161, 250, 126, 81, 91, 79, 221, 84, 11, 202, 229, 188, 169, 214, 92, 26, 201, 64, 55, 249, 108, 21, 189, 128, 87, 215, 157, 116, 77, 106, 78, 73, 41, 56, 245, 145, 154, 168, 253, 30, 141, 96, 207, 83, 33, 24, 2, 193, 65, 109, 153, 58};
unsigned char DECRYPT_MAPPING[] = {171, 163, 250, 121, 62, 43, 77, 112, 74, 3, 179, 212, 181, 90, 104, 53, 182, 12, 35, 162, 20, 225, 185, 132, 249, 116, 219, 140, 168, 153, 243, 97, 16, 248, 189, 122, 195, 159, 48, 142, 0, 236, 143, 28, 40, 49, 60, 78, 157, 197, 64, 92, 29, 128, 93, 222, 237, 113, 255, 84, 108, 76, 17, 131, 221, 252, 42, 19, 72, 8, 191, 133, 18, 235, 56, 9, 172, 232, 234, 209, 123, 207, 2, 247, 211, 89, 79, 228, 114, 91, 23, 208, 218, 5, 101, 71, 245, 176, 110, 129, 146, 161, 117, 22, 37, 15, 233, 118, 224, 253, 184, 175, 75, 192, 103, 52, 231, 67, 145, 115, 141, 31, 54, 109, 45, 190, 206, 202, 227, 106, 107, 68, 51, 120, 98, 111, 102, 137, 127, 50, 188, 244, 198, 30, 66, 239, 173, 130, 36, 63, 69, 193, 39, 254, 240, 1, 32, 230, 196, 160, 164, 204, 155, 167, 94, 26, 61, 33, 241, 216, 156, 59, 148, 47, 139, 81, 100, 174, 38, 203, 21, 186, 135, 13, 166, 70, 144, 10, 215, 226, 138, 85, 6, 251, 126, 152, 34, 87, 169, 149, 178, 220, 213, 73, 95, 83, 150, 246, 201, 57, 199, 4, 170, 194, 217, 229, 80, 86, 134, 11, 96, 210, 158, 147, 82, 187, 46, 124, 55, 214, 7, 99, 24, 105, 58, 136, 25, 177, 154, 200, 65, 44, 180, 88, 183, 238, 119, 41, 151, 223, 205, 27, 165, 242, 14, 125};
static ssize_t ts_read(int fd, void *aBuf, size_t nBuf);
static ssize_t ts_pread(int fd, void *aBuf, size_t nBuf, off_t off);
static ssize_t ts_pread64(int fd, void *aBuf, size_t nBuf, off64_t off);
static ssize_t ts_write(int fd, const void *aBuf, size_t nBuf);
static ssize_t ts_pwrite(int fd, const void *aBuf, size_t nBuf, off_t off);
static ssize_t ts_pwrite64(int fd, const void *aBuf, size_t nBuf, off64_t off);

struct Syscall {
    const char *zName;
    sqlite3_syscall_ptr xRepl;
    sqlite3_syscall_ptr xOrig;
};

struct Syscall aSyscall[] = {
        /*  0 */ { "read",      (sqlite3_syscall_ptr)ts_read,   (sqlite3_syscall_ptr) 0},
        /*  1 */ { "pread",     (sqlite3_syscall_ptr)ts_pread,  (sqlite3_syscall_ptr) 0},
        /*  2 */ { "pread64",   (sqlite3_syscall_ptr)ts_pread64,    (sqlite3_syscall_ptr) 0},
        /*  3 */ { "write",     (sqlite3_syscall_ptr)ts_write,  (sqlite3_syscall_ptr) 0},
        /*  4 */ { "pwrite",    (sqlite3_syscall_ptr)ts_pwrite, (sqlite3_syscall_ptr) 0},
        /*  5 */ { "pwrite64",  (sqlite3_syscall_ptr)ts_pwrite64,   (sqlite3_syscall_ptr) 0},
};

#define orig_read      ((ssize_t(*)(int,void*,size_t))aSyscall[0].xOrig)
#define orig_pread     ((ssize_t(*)(int,void*,size_t,off_t))aSyscall[1].xOrig)
#define orig_pread64   ((ssize_t(*)(int,void*,size_t,off64_t))aSyscall[2].xOrig)
#define orig_write     ((ssize_t(*)(int,const void*,size_t))aSyscall[3].xOrig)
#define orig_pwrite    ((ssize_t(*)(int,const void*,size_t,off_t))aSyscall[4].xOrig)
#define orig_pwrite64  ((ssize_t(*)(int,const void*,size_t,off64_t))aSyscall[5].xOrig)


static ssize_t ts_read(int fd, void *aBuf, size_t nBuf) {
    ssize_t ret = orig_read(fd, aBuf, nBuf);
    for (int i = 0; i < ret; i++) {
        unsigned char * tmp_buf = (unsigned char *) aBuf;
        tmp_buf[i] = DECRYPT_MAPPING[tmp_buf[i]];
    }
    return ret;
}


static ssize_t ts_write(int fd, const void *aBuf, size_t nBuf) {
    unsigned char encrypt_buffer[nBuf];
    for (int i = 0; i < nBuf; i++) {
        encrypt_buffer[i] = ENCRYPT_MAPPING[((unsigned char *) aBuf)[i]];
    }
    ssize_t ret = orig_write(fd, encrypt_buffer, nBuf);
    return ret;
}



static ssize_t ts_pread(int fd, void *aBuf, size_t nBuf, off_t off){
    ssize_t ret = orig_pread(fd, aBuf, nBuf, off);
    for (int i = 0; i < ret; i++) {
        unsigned char * tmp_buf = (unsigned char *) aBuf;
        tmp_buf[i] = DECRYPT_MAPPING[tmp_buf[i]];
    }
    return ret;
}


static ssize_t ts_pread64(int fd, void *aBuf, size_t nBuf, off64_t off){
    ssize_t ret = orig_pread64(fd, aBuf, nBuf, off);
    for (int i = 0; i < ret; i++) {
        unsigned char * tmp_buf = (unsigned char *) aBuf;
        tmp_buf[i] = DECRYPT_MAPPING[tmp_buf[i]];
    }
    return ret;
}


static ssize_t ts_pwrite(int fd, const void *aBuf, size_t nBuf, off_t off){
    unsigned char encrypt_buffer[nBuf];
    for (int i = 0; i < nBuf; i++) {
        encrypt_buffer[i] = ENCRYPT_MAPPING[((unsigned char *) aBuf)[i]];
    }
    ssize_t ret = orig_pwrite(fd, encrypt_buffer, nBuf, off);
    return ret;
}


static ssize_t ts_pwrite64(int fd, const void *aBuf, size_t nBuf, off64_t off){
    unsigned char encrypt_buffer[nBuf];
    for (int i = 0; i < nBuf; i++) {
        encrypt_buffer[i] = ENCRYPT_MAPPING[((unsigned char *) aBuf)[i]];
    }
    ssize_t ret = orig_pwrite64(fd, encrypt_buffer, nBuf, off);
    return ret;
}

static void replace_sqlite_syscall() {
    void* handle = dlopen("libsqlite.so", RTLD_LAZY);
    sqlite3_vfs* (*sqlite3_vfs_find)(const char*) = (sqlite3_vfs* (*)(const char* name)) dlsym(handle, "sqlite3_vfs_find");
    int nElem = sizeof(aSyscall) / sizeof(struct Syscall);
    sqlite3_vfs* pVfs = sqlite3_vfs_find("unix");
    for(int i=0; i<nElem; i++){
        aSyscall[i].xOrig = pVfs->xGetSystemCall(pVfs, aSyscall[i].zName);
        pVfs->xSetSystemCall(pVfs, aSyscall[i].zName, aSyscall[i].xRepl);
    }
    dlclose(handle);
}

static bool verifyPackageName(JNIEnv* env, jobject context) {
    jclass android_content_Context =env->GetObjectClass(context);
    jmethodID midGetPackageName = env->GetMethodID(android_content_Context,"getPackageName", "()Ljava/lang/String;");
    jstring packageName= (jstring)env->CallObjectMethod(context, midGetPackageName);
    const char *nativePackageName = env->GetStringUTFChars(packageName, 0);
    bool valid = strcmp(nativePackageName, "com.madeinhk.english_chinesedictionary") == 0;
    env->ReleaseStringUTFChars(packageName, nativePackageName);
    return valid;
}

extern "C" {

JNIEXPORT void JNICALL Java_com_madeinhk_utils_Obfuscator_init(JNIEnv * env, jclass cls, jobject context) {
  bool valid = verifyPackageName(env, context);
    if (!valid) {
      exit(-1);
  }
    replace_sqlite_syscall();
}
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}
