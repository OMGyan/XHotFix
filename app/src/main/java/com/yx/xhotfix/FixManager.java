package com.yx.xhotfix;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Author by YX, Date on 2019/10/25.
 * 修复Dex文件的工具类
 */
public class FixManager {

    //创建一个用来存储加载到的dex文件的集合
    private static HashSet<File> loadedDexSet = new HashSet<>();

    static {
        //保证集合操作前为空
        loadedDexSet.clear();
    }

    /**
     * 根据上下文来加载dex文件，并放入集合中
     * @param context
     */
    public static void loadDex(Context context){
        if(context == null){
            return;
        }
        //获取当前应用所在私有路径,也就是dex文件的目录
        File odexDir = context.getDir("odex", Context.MODE_PRIVATE);
        //通过该目录获得目录下所有文件的数组
        File[] files = odexDir.listFiles();
        for (File file : files) {
            if(file.getName().startsWith("classes")||file.getName().endsWith(".dex")){
                loadedDexSet.add(file);
            }
        }
        //创建一个目录,用来装载解压的文件
        String optimizeDir = odexDir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizeDir);
        //如果这个目录不存在就创建
        if(!fopt.exists()){
            fopt.mkdirs();
        }
        //遍历这个dex集合
        for (File file : loadedDexSet) {
            //获取当前dex类加载器
            DexClassLoader dexClassLoader = new DexClassLoader(file.getAbsolutePath(), fopt.getAbsolutePath(), null, context.getClassLoader());
            //实现一个类加载器的对象
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            try {
                //通过反射拿到系统类加载器
                Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
                Field systemPathList = baseDexClassLoaderClass.getDeclaredField("pathList");
                systemPathList.setAccessible(true);
                Object splObj = systemPathList.get(pathClassLoader);
                Class<?> pathListClass = splObj.getClass();
                Field dexElements = pathListClass.getDeclaredField("dexElements");
                dexElements.setAccessible(true);
                Object dexElementsObj = dexElements.get(splObj);

                //创建自己的类加载器
                Class<?> myBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
                Field myPathList = myBaseDexClassLoaderClass.getDeclaredField("pathList");
                myPathList.setAccessible(true);
                Object mysplObj = myPathList.get(dexClassLoader);
                Class<?> myPathListClass = mysplObj.getClass();
                Field myDexElements = myPathListClass.getDeclaredField("dexElements");
                myDexElements.setAccessible(true);
                Object mydexElementsObj = myDexElements.get(mysplObj);

                //进行dex文件的融合
                Class<?> componentType = dexElementsObj.getClass().getComponentType();
                //分别得到两个dexElements的长度
                int systemDEL = Array.getLength(dexElementsObj);
                int myDEL = Array.getLength(mydexElementsObj);
                //创建一个能放入它们的数组
                int newL = systemDEL + myDEL;
                Object newDEL = Array.newInstance(componentType, newL);
                for (int i = 0; i < newL; i++) {
                    if(i < myDEL){
                        Array.set(newDEL,i,Array.get(mydexElementsObj,i));
                    }else {
                        Array.set(newDEL,i,Array.get(dexElementsObj,i-myDEL));
                    }
                }
                //将融合后的数组赋值给系统
                Field systemDexElements = pathListClass.getDeclaredField("dexElements");
                systemDexElements.setAccessible(true);
                systemDexElements.set(splObj,newDEL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
