## 如何使用Xposed进行inlinHook Native

思路分析-> 参考文章 https://www.jianshu.com/p/614721f7a2c8

要进行inlinHook最重要的就是要植入我们的hookSo并且链接起来

在安装了XP的情况下-思路如下

1.利用xposed hook  so load 函数加载
2.植入hook so 链接到目标程序
3.hook so 进行 inlineHook

下面对各个步骤开始分析


## 0.制作HookSo

首先我们把一些成熟的inline hook 框架打进一个so里面 具体看 CMakeLists.txt的链接文件


### inlineHook 核心库文件

具体包括: Substrate 和 inlineHook dlfc 这些框架和工具

http://www.cydiasubstrate.com/
https://github.com/ele7enxxh/Android-Inline-Hook
https://github.com/lizhangqu/dlfcn_compat

```
#        Substrate
        Substrate/hde64.c
        Substrate/SubstrateDebug.cpp
        Substrate/SubstrateHook.cpp
        Substrate/SubstratePosixMemory.cpp

        #        inlineHook
        dlfc/dlfcn_nougat.cpp
        dlfc/dlfcn_compat.cpp
        inlineHook/inlineHook.cpp
        inlineHook/relocate.cpp
```

### 具体hook逻辑类

这部分写在main.cpp 里面 主要是写了如何hook的目标方法的逻辑

这部分放到后面讲解

编译后我们的so文件就叫`libLVmp.so`,现在他在我们自己的apk目录下。接下来要放到目标的app进程里面


## 1.目标程序加载hook SO

### 加载时机 可以选择 hook Activity

```java
XposedHelpers.findAndHookMethod(Activity.class,
                "onCreate",
                Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (isNeedHookNative) {
                    intoMySo(null);
                }
            }
        });
```


### 找到hook模块的so文件路径

```
/**
     * 获取模块的So文件路径
     */
    public static String getMySoPath() {
        CLogUtils.e("开始注入自己的 So getMySoPath   ");
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> pkgList = pm.getInstalledPackages(0);
        if (pkgList.size() > 0) {
            for (PackageInfo pi : pkgList) {
                if (pi.applicationInfo.publicSourceDir.startsWith("/data/app/" + BuildConfig.APPLICATION_ID)) {
                    String path = pi.applicationInfo.publicSourceDir.
                            replace("base.apk", "lib/arm/libLVmp.so");
                    CLogUtils.e("getMySoPath 对应的路径是" + path);
                    return path;
                }
            }
        }
        CLogUtils.e("没找到 MySo注入的 路径 ");
        return null;
    }
```

### 加载so文件


这里的加载逻辑就是SoLoad

9.0以上的System.load 走的就是这里

Runtime.java

```
private static String nativeLoad(String filename, ClassLoader loader) {
        return nativeLoad(filename, loader, null);
    }

private static native String nativeLoad(String filename, ClassLoader loader, Class<?> caller);
```

所以利用Xposed调用该方法(第三个参数没啥意义)

intoMySo(null);

```
/**
     * 在这里 把自己的 so进行 注入
     *
     * @param arg
     */
    public static void intoMySo(Object arg) {
        String path = getMySoPath();
        int version = android.os.Build.VERSION.SDK_INT;
        CLogUtils.e("当前系统 版本号 " + version);
        //android 9.0没有 doLoad 方法
        if (version >= 28) {
            XposedHelpers.callMethod(Runtime.getRuntime(), "nativeLoad", path, arg);
        }else {
            XposedHelpers.callMethod(Runtime.getRuntime(), "doLoad", path, arg);
        }
        CLogUtils.e("intoMySo 注入成功");
    }
```

## 实施 inlinHook

现在回到我们的 main.cpp 文件制作我们的hook逻辑

### inlinHook流程

1.JNI_OnLoad 进入 hook入口
2.使用 dlopen_compat 函数获取目标so文件的基址(so文件在内存里的起始地址) 使用全路径或者特征寻找打开
3.使用 dlsym_compat 函数 打开so 传入方法名 寻找hook的目标函数 的源地址
4.使用 registerInlineHook 注册一个InlineHook 方法 传入  第一个就是你要hook的目标函数地址，第二个是自己的替换函数指针，第三个是保留函数原来的指针

具体实现看代码

核心的hook

```c
jstring My_Java_com_huruwo_behook_MainActivity_md5(JNIEnv *env, jobject thiz, jstring str) {
    LOG(ERROR) << "插件Hook成功-------------";
    LOG(ERROR) << "函数参数Hook:" << parse::jstring2str(env, str);
    auto result = Source_Java_com_huruwo_behook_MainActivity_md5(env, thiz, env->NewStringUTF("0000"));
    LOG(ERROR) << "函数结果Hook:" << parse::jstring2str(env,result);
    return env->NewStringUTF("xxx");
}
```


## 项目源码地址