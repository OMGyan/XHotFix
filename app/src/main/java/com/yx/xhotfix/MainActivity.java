package com.yx.xhotfix;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }



    public void goCalculation(View view) {
          Test test = new Test();
          test.add(this);
    }

    public void goHotFix(View view) {
        //获取到apk的私有存储路径
        File filesDir = getDir("odex", Context.MODE_PRIVATE);
        //获取到没有bug的dex文件的名字
        String name = "out.dex";
        //创建该dex文件的file
        String path = new File(filesDir, name).getAbsolutePath();
        //根据这个路径去创建一个新的file对象
        File file = new File(path);
        //如果这个文件存在就删除掉
        if(file.exists()){
            file.delete();
        }
        //创建io流
        InputStream is = null;
        FileOutputStream os = null;
        try {
           is = new FileInputStream(new File(Environment.getExternalStorageDirectory(),name));
           os = new FileOutputStream(path);
           int len = 0;
           byte[] bytes = new byte[1024];
           while ((len = is.read(bytes))!=-1){
                os.write(bytes,0,len);
           }
            File f  = new File(path);
           if(f.exists()){
               Toast.makeText(this, "修复成功", Toast.LENGTH_SHORT).show();
           }
           FixManager.loadDex(this);
        }catch (Exception e){
           e.printStackTrace();
        }finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
