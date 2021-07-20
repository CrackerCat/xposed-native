package com.huruwo.xposednative.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import dalvik.system.DexClassLoader;

/**
 * @author Zhenxi on 2020-07-22
 */
public class FixElementsUtils {

    private static String INTERCEPTORPATH = "/storage/emulated/0/MyDex/Test.dex";
    private static String OUTRPATH = "/storage/emulated/0/MyDex/Out/Test.dex";


    public static boolean CopyFile(Context context) {
        CLogUtils.e("开始执行 CopyFile");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "MyDex";
        String Outpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "MyDex" + "/" + "Out";
        File file = new File(path);
        File file1 = new File(Outpath);
//        if (file.exists()) {
//            CLogUtils.e("文件已存在 ");
//            file.delete();
//        }
        boolean mkdirs = file.mkdirs();
        boolean mkdirs1 = file1.mkdirs();
        CLogUtils.e("是否成功 "+mkdirs+" "+mkdirs1);
        return Assets2Sd(context, "Test.dex", path + "/" + "Test.dex");
    }

    /***
     * 调用方式
     *
     * String path = Environment.getExternalStorageDirectory().toString() + "/" + "Tianchaoxiong/useso";
     String modelFilePath = "Model/seeta_fa_v1.1.bin";
     Assets2Sd(this, modelFilePath, path + "/" + modelFilePath);
     *
     * @param context
     * @param fileAssetPath assets中的目录
     * @param fileSdPath 要复制到sd卡中的目录
     */
    public static boolean Assets2Sd(Context context, String fileAssetPath, String fileSdPath) {
        CLogUtils.e("开始执行 Assets2Sd");
        //测试把文件直接复制到sd卡中 fileSdPath完整路径
        try {
            return copyBigDataToSD(context, fileAssetPath, fileSdPath);
        } catch (IOException e) {
            CLogUtils.e("************拷贝失败");
            return false;
        }

    }

    public static boolean copyBigDataToSD(Context context, String fileAssetPath, String strOutFileName) throws IOException {
        CLogUtils.e("开始执行 copyBigDataToSD");

        try {
            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(strOutFileName);
            myInput = context.getAssets().open(fileAssetPath);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
            return true;
        } catch (IOException e) {
            CLogUtils.e("copyBigDataToSD error " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * @return 添加 Dex 并且判断 是否合并 成功
     */
    public static boolean AddElements(Context context, ClassLoader mloader) {

        CLogUtils.e("开始执行AddElements");
        File dexOutputDir = context.getDir("dex", 0);

        //先创建加载自定义Dex  ClassLoader的方法
        DexClassLoader mDexClassLoader = new DexClassLoader(INTERCEPTORPATH,
                dexOutputDir.getAbsolutePath(), null, mloader);

        //自己的 classloader 里面的 element数组
        Object[] myDexClassLoaderElements = getClassLoaderElements(mDexClassLoader);
        if (myDexClassLoaderElements == null) {
            CLogUtils.e("AddElements  myDexClassLoaderElements null");
            return false;
        } else {
            CLogUtils.e("AddElements  成功 拿到 myDexClassLoaderElements 自己的Elements 长度是   " + myDexClassLoaderElements.length);
        }

        //系统的  classloader 里面的 element数组
        Object[] classLoaderElements = getClassLoaderElements(mloader);
        //将数组合并
        if (classLoaderElements == null) {
            CLogUtils.e("AddElements  classLoaderElements null");
            return false;
        } else {
            CLogUtils.e("AddElements  成功 拿到 classLoaderElements 系统的Elements 长度是   " + classLoaderElements.length);
        }

        //DexElements合并
        Object[] combined = (Object[]) Array.newInstance(classLoaderElements.getClass().getComponentType(),
                classLoaderElements.length + myDexClassLoaderElements.length);

        System.arraycopy(classLoaderElements, 0, combined, 0, classLoaderElements.length);
        System.arraycopy(myDexClassLoaderElements, 0, combined, classLoaderElements.length, myDexClassLoaderElements.length);


        //Object[] dexElementsResut = concat(myDexClassLoaderElements, classLoaderElements);

        if ((classLoaderElements.length + myDexClassLoaderElements.length) != combined.length) {
            CLogUtils.e("合并 elements数组 失败  null");
        }
        //合并成功 重新 加载
        return SetDexElements(mloader, combined, myDexClassLoaderElements.length + classLoaderElements.length);
    }


    /**
     * 将自己 创建的 classloader 里面的 内容添加到 原来的 classloader里面
     *
     * @param mloader
     */
    private static Object[] getClassLoaderElements(ClassLoader mloader) {
        try {
            Field pathListField = mloader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mloader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    if (dexElements != null) {
                        return dexElements;
                    } else {
                        CLogUtils.e("AddElements  获取 dexElements == null");
                    }
                    //ArrayUtils.addAll(first, second);
                } else {
                    CLogUtils.e("AddElements  获取 dexElements == null");
                }
            } else {
                CLogUtils.e("AddElements  获取 pathList == null");
            }
        } catch (NoSuchFieldException e) {
            CLogUtils.e("AddElements  NoSuchFieldException   " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            CLogUtils.e("AddElements  IllegalAccessException   " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 将 Elements 数组 set回原来的 classloader里面
     *
     * @param mloader
     * @param dexElementsResut
     */
    private static boolean SetDexElements(ClassLoader mloader, Object[] dexElementsResut, int conunt) {
        try {
            Field pathListField = mloader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mloader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    //先 重新设置一次
                    dexElementsField.set(dexPathList, dexElementsResut);
                    //重新 get 用
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    if (dexElements.length == conunt && Arrays.hashCode(dexElements) == Arrays.hashCode(dexElementsResut)) {
                        CLogUtils.e("合并成功");
                        return true;
                    } else {
                        CLogUtils.e("合成   长度  " + dexElements.length + "传入 数组 长度   " + conunt);

                        CLogUtils.e("   dexElements hashCode " + Arrays.hashCode(dexElements) + "  " + Arrays.hashCode(dexElementsResut));

                        return false;
                    }
                } else {
                    CLogUtils.e("SetDexElements  获取 dexElements == null");
                }
            } else {
                CLogUtils.e("SetDexElements  获取 pathList == null");
            }
        } catch (NoSuchFieldException e) {
            CLogUtils.e("SetDexElements  NoSuchFieldException   " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            CLogUtils.e("SetDexElements  IllegalAccessException   " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
