
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

static const char *TAG = "123123";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)



//extern "C" JNIEXPORT jlong
//Java_com_jjj_client_AshmemReader_initAshmem(JNIEnv* env, jobject thiz, jstring path, jint size) {
//
//
//    const char* m_path= reinterpret_cast<const char *>(env->GetStringChars(path, 0));
//
//    // 常量字符
//    const char *parentPath = "/sdcard/";
//
//    // 计算拼接后字符串的长度
//    size_t totalLen = strlen(parentPath) + strlen(m_path) + 1; // +1 用于存放字符串结束符 '\0'
//    // 创建一个字符数组来存放拼接后的字符串
//    char result[totalLen];
//    // 拷贝常量字符到结果数组
//    strcpy(result, parentPath);
//    // 使用strcat拼接Java字符串到结果数组
//    strcat(result, m_path);
//
//    // 打开Ashmem文件描述符
//    int shared_fd = open(result, O_RDWR);
//    if (shared_fd < 0) {
//        return -1;
//    }
//
//    // 设置Ashmem区域的大小
//    if (ioctl(shared_fd, ASHMEM_SET_SIZE, size) < 0) {
//        close(shared_fd);
//        return -1;
//    }
//
//    // 映射Ashmem到内存
//    jlong shared_memory_ptr   = reinterpret_cast<jlong>(mmap(NULL, size, PROT_READ,
//                                                        MAP_SHARED, shared_fd, 0));
//    if (shared_memory_ptr  == 0) {
//        close(shared_fd);
//        return -1;
//    }
//
//    env->ReleaseStringUTFChars(path,m_path);
//
//    return shared_memory_ptr ;
//}


extern "C" JNIEXPORT jlong
Java_com_jjj_client_AshmemReader_initAshmemByFd(JNIEnv *env, jobject thiz, jint shared_fd,
                                                jint size) {


    LOGE("client mmap SIZE:%d", size);
    LOGE("client mmap shared_fd:%ld", shared_fd);

    // 映射Ashmem到内存
    void *shared_memory_ptr = mmap(NULL, size, PROT_WRITE,
                                   MAP_SHARED, shared_fd, 0);


    LOGE("client shared_memory_ptr:%ld", shared_memory_ptr);
    if (shared_memory_ptr == MAP_FAILED) {

        // 获取错误代码
        int error_code = errno;
        // 使用错误代码来查找错误描述
        const char *error_description = strerror(error_code);
        // 打印错误描述（仅用于调试）
        LOGE("错误代码：%d，错误描述：%s\n", error_code, error_description);


        int ret = close(shared_fd);
        LOGE("client close ret:%d", ret);
        return -1;
    }

    return reinterpret_cast<jlong>(shared_memory_ptr);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_jjj_client_AshmemReader_readAshmemData(JNIEnv *env, jobject thiz, jlong shared_memory_ptr,
                                                jint offset, jint size, jbyteArray buffer) {

    if (shared_memory_ptr == 0) {
        return;
    }


    jbyte *data_ptr = env->GetByteArrayElements(buffer, NULL);
    if (data_ptr == NULL) {
        return;
    }

    memcpy(data_ptr, (char *) shared_memory_ptr + offset, size);

    return;
}