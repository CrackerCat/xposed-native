package com.huruwo.xposednative.utils;

import com.huruwo.xposednative.utils.CLogUtils;
import com.huruwo.xposednative.utils.FixElementsUtils;

import static com.huruwo.xposednative.HookMain.AppAllCLassLoaderList;
import static com.huruwo.xposednative.HookMain.mContext;
import static com.huruwo.xposednative.HookMain.mLoader;

public class HookUtils {

    /**
     * 遍历当前进程的Classloader 尝试进行获取指定类
     */
    public static Class getClass(String className) {
        Class<?> aClass = null;
        try {
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException classNotFoundE) {

                try {
                    aClass = Class.forName(className, false, mLoader);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (aClass != null) {
                    return aClass;
                }
                try {
                    for (ClassLoader classLoader : AppAllCLassLoaderList) {
                        try {
                            aClass = Class.forName(className, false, classLoader);
                        } catch (Throwable e) {
                            continue;
                        }
                        if (aClass != null) {
                            return aClass;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            return aClass;
        } catch (Throwable e) {

        }
        return null;
    }

    /**
     * 将自己的Dex 注入到对方进程
     * 利用父委托机制 替换掉源码里面的class
     */
    public static void intoMyCode() {
        boolean addElements = FixElementsUtils.AddElements(mContext, mLoader);
        CLogUtils.e("AddElements执行结果 "+addElements);

    }

    private void HookMianInit() {
//        if (ClassPath != null && !ClassPath.equals("")) {
//            try {
//                bin = Class.forName(ClassPath, true, mLoader);
//                //bin2 = Class.forName(ClassPath2, true, mLoader);
////                            bin3 = Class.forName(ClassPath3, true, mLoader);
//                if (bin == null) {
//                    bin = Class.forName(ClassPath);
//                    //bin2 = Class.forName(ClassPath2);
////                                bin3 = Class.forName(ClassPath3);
//                }
//                if (bin != null) {
//                    CLogUtils.e("成功拿到 bin  ");
//                    HookMain();
//                } else {
//                    CLogUtils.e("没有拿到bin 对象");
//                }
//            } catch (Throwable e) {
//                e.printStackTrace();
//                CLogUtils.e("没找到 Bin  开始执行 forEatchClassLoader   " + e.getMessage());
//                for (ClassLoader mLoader2 : AppAllCLassLoaderList) {
//                    try {
//                        bin = Class.forName(ClassPath, true, mLoader2);
//                        if (bin != null) {
//                            HookMain();
//                            return;
//                        }
//                    } catch (ClassNotFoundException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//                CLogUtils.e("forEatchClassLoader 循环完毕也没找到");
//            }
//        }
    }

}
