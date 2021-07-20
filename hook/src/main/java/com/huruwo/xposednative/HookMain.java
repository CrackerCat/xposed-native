package com.huruwo.xposednative;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;


import com.huruwo.xposednative.utils.CLogUtils;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.huruwo.xposednative.hook.HookSoHelper.intoMySo;

//import ctrip.business.BusinessRequestEntity;
//import ctrip.business.comm.RequestDataBean;


public class HookMain implements IXposedHookLoadPackage {


    public static boolean isNeedIntoMyDex = true;
    public static Context mContext;
    public static ClassLoader mLoader;
    public static Activity topActivity;
    private static Object GSON = null;
    private static View All = null;
    private static View verifyImageView = null;
    //方法所属class路径
    public static String ClassPath = "";
    public static String MethodName = "";
    //匹配对方so的名字
    public static final String IntoSoName = "libTest.so";
    public static String packageName = "com.huruwo.behook";
    //是否需要HookNaitive方法的 开关
    private boolean isNeedHookNative = true;
    private Class bin;
    //存放 这个 app全部的 classloader
    public static ArrayList<ClassLoader> AppAllCLassLoaderList = new ArrayList<>();
    private Activity mActivity;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(packageName)) {
            CLogUtils.e("发现被Hook的 App");
            hookLoadClass();
            hookAttach();
        }
    }

    private void hookLoadClass() {
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (param == null) {
                            return;
                        }
                        Class cls = (Class) param.getResult();
                        if (cls == null) {
                            return;
                        }
                        isNeedAddClassloader(cls.getClassLoader());
                    }
                });

        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass",
                String.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (param == null) {
                            return;
                        }
                        Class cls = (Class) param.getResult();
                        if (cls == null) {
                            return;
                        }
                        isNeedAddClassloader(cls.getClassLoader());
                    }
                });
    }

    private void isNeedAddClassloader(ClassLoader classLoader) {

        //先过滤掉系统类 ,如果是系统预装的 类也 不要
        if (classLoader.getClass().getName().equals("java.lang.BootClassLoader")) {
            return;
        }
        for (ClassLoader loader : AppAllCLassLoaderList) {
            if (loader.hashCode() == classLoader.hashCode()) {
                return;
            }
        }
        //CLogUtils.e("加入的classloader名字  " + classLoader.getClass().getName());
        AppAllCLassLoaderList.add(classLoader);
    }

    private void hookAttach() {
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                CLogUtils.e("走了 attachBaseContext方法 ");
                mContext = (Context) param.args[0];
                mLoader = mContext.getClassLoader();
                CLogUtils.e("拿到classloader");
            }
        });
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
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                mActivity = (Activity) param.thisObject;
            }
        });

    }
}

