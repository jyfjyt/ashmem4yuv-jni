
#include <jni.h>
#include <sys/mman.h>
#include <linux/ashmem.h>

#include <string>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>

#include "android/log.h"

static const char *TAG = "123123jni";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)





extern "C" JNIEXPORT jint
Java_com_jjj_server_AshmemWriter_createAshmem(JNIEnv *env, jobject thiz, jstring path, jint size) {


    const char *m_path = env->GetStringUTFChars(path, 0);

    // 常量字符
    const char *parentPath = "/dev/ashmem";

    // 打开Ashmem文件描述符
    int shared_fd = open(parentPath, O_RDWR);
    if (shared_fd < 0) {
        LOGE("open:%d", shared_fd);
        return -1;
    }

    ioctl(shared_fd, ASHMEM_SET_NAME,m_path);
    ioctl(shared_fd, ASHMEM_SET_SIZE, size);

    env->ReleaseStringUTFChars(path, m_path);

    return shared_fd;
}



extern "C" JNIEXPORT jlong
Java_com_jjj_server_AshmemWriter_initAshmemByFd(JNIEnv *env, jobject thiz, jint shared_fd,
                                                  jint size) {

    // 映射Ashmem到内存
    void *shared_memory_ptr;
    shared_memory_ptr= mmap(NULL, size, PROT_WRITE,
                                                           MAP_SHARED , shared_fd, 0);


    LOGE("sercer shared_memory_ptr:%ld", shared_memory_ptr);
    if (shared_memory_ptr == MAP_FAILED) {

        // 获取错误代码
        int error_code = errno;
        // 使用错误代码来查找错误描述
        const char *error_description = strerror(error_code);
        // 打印错误描述（仅用于调试）
        LOGE("错误代码：%d，错误描述：%s\n", error_code, error_description);

        int ret = close(shared_fd);
        LOGE("sercer close ret:%d", ret);
        return -1;
    }

    return reinterpret_cast<jlong>(shared_memory_ptr);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_jjj_server_AshmemWriter_writeAshmemData(JNIEnv *env, jobject thiz, jlong shared_memory_ptr,
                                                 jint offset, jbyteArray buffer,
                                                 jint length) {

    if (shared_memory_ptr <= 0) {
        LOGE("write NULL");
        return -1;
    }

    jbyte *data = env->GetByteArrayElements(buffer, NULL);
    if (data == NULL) {
        LOGE("write NULL");
        return -1;
    }

    // 将数据写入共享内存
    memcpy((char *)shared_memory_ptr+offset, data, length);
//    LOGE("write memcpyed");
    env->ReleaseByteArrayElements(buffer, data, 0);

    return 0;
}


//---------------------------------------------
//---------------------------------------------
//---------------------------------------------
//---------------------------------------------
//---------------------------------------------
//
//
//
//
//extern "C" JNIEXPORT jlong
//Java_com_jjj_client_AshmemReader_initAshmemByFd(JNIEnv *env, jobject thiz, jint shared_fd,
//                                                jint size) {
//
//
//    LOGE("client mmap SIZE:%d",size);
//    LOGE("client mmap shared_fd:%ld",shared_fd);
//
//    // 映射Ashmem到内存
//    void *shared_memory_ptr= mmap(NULL, size, PROT_WRITE,
//                                                          MAP_SHARED , shared_fd, 0);
//
//
//    LOGE("client shared_memory_ptr:%ld", shared_memory_ptr);
//    if (shared_memory_ptr == MAP_FAILED) {
//
//        // 获取错误代码
//        int error_code = errno;
//        // 使用错误代码来查找错误描述
//        const char *error_description = strerror(error_code);
//        // 打印错误描述（仅用于调试）
//        LOGE("错误代码：%d，错误描述：%s\n", error_code, error_description);
//
//
//        int ret = close(shared_fd);
//        LOGE("client close ret:%d", ret);
//        return -1;
//    }
//
//    return reinterpret_cast<jlong>(shared_memory_ptr);
//}
//
//
//extern "C"
//JNIEXPORT jbyteArray JNICALL
//Java_com_jjj_client_AshmemReader_readAshmemData(JNIEnv *env, jobject thiz, jlong shared_memory_ptr,
//                                                jint offset, jint size) {
//
//    if (shared_memory_ptr == 0) {
//        return NULL;
//    }
//
//    // 从共享内存中读取数据，使用偏移量和长度
//    jbyteArray data = env->NewByteArray(size);
//    if (data == NULL) {
//        // 处理错误
//        return NULL;
//    }
//
//    jbyte *data_ptr = env->GetByteArrayElements(data, NULL);
//    if (data_ptr == NULL) {
//        return NULL;
//    }
//
//    memcpy(data_ptr, (char *) shared_memory_ptr + offset, size);
//
//    return data;
//}