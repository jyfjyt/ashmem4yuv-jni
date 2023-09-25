package com.jjj.server;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends Activity {

    static {
        System.loadLibrary("ashmem-sdk");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("123123", "main:" + android.os.Process.myPid());
        setContentView(R.layout.activity_main);
        checkPermission();

//
//        findViewById(R.id.run0).setOnClickListener(v -> {
//            AshmemService.mIndex=0;
//            startService(new Intent(this, AshmemService.class));
//            v.setEnabled(false);
//        });
//
//        findViewById(R.id.run1).setOnClickListener(v -> {
//            AshmemService.mIndex=1;
//            startService(new Intent(this, AshmemService.class));
//            v.setEnabled(false);
//        });
//
//
//        findViewById(R.id.run2).setOnClickListener(v -> {
//            AshmemService.mIndex=2;
//            startService(new Intent(this, AshmemService.class));
//            v.setEnabled(false);
//        });
//
//        findViewById(R.id.run3).setOnClickListener(v -> {
//            AshmemService.mIndex=3;
//            startService(new Intent(this, AshmemService.class));
//            v.setEnabled(false);
//        });

        startService(new Intent(this, AshmemService.class));

//        findViewById(R.id.debug).setOnClickListener(v -> startActivity(new Intent(this, ClientMainActivity.class)));
    }


    //相机、读写权限
    void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }

}