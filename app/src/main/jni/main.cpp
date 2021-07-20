

#include <string>
#include <regex>
#include <bits/getopt.h>
#include "main.h"
#include "encrypt/MyMD5.h"
#include "utils/parse.h"



//jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
//    LOG(ERROR) << "JNI_OnLoad 开始加载"; //动态注册函数入口
//    //在 onload 改变 指定函数 函数地址 替换成自己的
//    JNIEnv *env = nullptr;
//    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
//        return JNI_VERSION_1_6;
//    }
//    return 0;
//}

char* secret= nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_huruwo_behook_MainActivity_md5(JNIEnv *env, jobject thiz, jstring str) {
    //将jstring转化成jstring类型
    const string &basicString = parse::jstring2str(env, str);

    MyMD5 md5 = MyMD5(basicString+secret);

    std::string md5Result = md5.hexdigest();

    LOG(ERROR) << " MD5 返回结果  "<< md5Result;

    //将char *类型转化成jstring返回给Java层
    return env->NewStringUTF(md5Result.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_huruwo_behook_MainActivity_init(JNIEnv *env, jclass clazz) {
    secret="abcd";
}