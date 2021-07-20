

#include "main.h"


void into_TestSo();

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    LOG(ERROR) << "插件 JNI_OnLoad 开始加载 ";
    //在 onload 改变 指定函数 函数地址 替换成自己的
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {

        into_TestSo();

        return JNI_VERSION_1_6;
    }

    return 0;
}

//获取 ArtSo基地址 函数
// 从 lib64和普通的lib里面去寻找
void *getTestSo() {
    //1.看对应的Map文件
    //2.MT管理器去 data/app 下面
    //so路径需注意
    auto *pLibart = dlopen_compat("com.huruwo.behook|libTest.so", RTLD_NOW);

//    auto *pLibart = dlopen_compat(
//            "/data/app/com.kejian.one-X5q5ERrm-bW9b98-rRoKvQ==/lib/arm/libTest.so", RTLD_NOW);

    if (pLibart != nullptr) {
        LOGE("拿到 TestSo.so");
        return pLibart;
    }
    return nullptr;
}


jstring My_Java_com_huruwo_behook_MainActivity_md5(JNIEnv *env, jobject thiz, jstring str) {
    LOG(ERROR) << "插件Hook成功-------------";
    LOG(ERROR) << "函数参数Hook:" << parse::jstring2str(env, str);
    str = env->NewStringUTF("0000");
    auto result = Source_Java_com_huruwo_behook_MainActivity_md5(env, thiz, str);
    LOG(ERROR) << "函数结果Hook:" << parse::jstring2str(env,result);
    result = env->NewStringUTF("xxx");
    return result;
}


void into_TestSo() {
    //先拿到对应的So文件句柄
    void *TestSoLib = getTestSo();
    if (TestSoLib != nullptr) {
        LOG(ERROR) << "拿到  TestSo 句柄";

        void *Test_MD5 = dlsym_compat(TestSoLib,
                                      "Java_com_huruwo_behook_MainActivity_md5");
        if (Test_MD5) {
            LOG(ERROR) << "Test_MD5 成功";
            //1.我们拿到的原函数的地址
            //2.我们自己实现对应函数的函数地址
            //3.被Hook原函数地址
            if (ELE7EN_OK == registerInlineHook((uint32_t) Test_MD5,
                                                (uint32_t) My_Java_com_huruwo_behook_MainActivity_md5,
                                                (uint32_t **) &Source_Java_com_huruwo_behook_MainActivity_md5)) {
                if (ELE7EN_OK == inlineHook((uint32_t) Test_MD5)) {
                    LOGE("inlineHook Test_MD5 success");
                }
            }
        } else{
            LOG(ERROR) << "Test_MD5 失败";
        }
    }

}





