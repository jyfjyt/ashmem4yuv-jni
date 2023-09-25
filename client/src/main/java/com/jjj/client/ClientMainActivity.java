package com.jjj.client;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.jjj.client.databinding.ClientActivityMainBinding;
import com.jjj.server.IAshmem;

import androidx.appcompat.app.AppCompatActivity;

public class ClientMainActivity extends AppCompatActivity {


    private ClientActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();

        Log.i("123123", "client main:" + android.os.Process.myPid());
        binding = ClientActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.de1.setOnClickListener(v -> {
            bind();
        });
    }

    void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
        }
    }


    private boolean isConnect = false;
    private IAshmem mAshmem = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAshmem = IAshmem.Stub.asInterface(service);
            try {
                mAshmem.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                binding.de1.setText("Connected");
            });
            isConnect = true;

            new Thread(() -> doAction()).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnect = false;
            runOnUiThread(() -> {
                binding.de1.setText("Dis Connect");
            });
        }
    };


    // 失效重联机制, 当Binder死亡时, 重新连接
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            runOnUiThread(() -> {
                binding.de1.setText("ERR DEATH");
            });
        }
    };

    private void bind() {
        Intent intent = new Intent();
        intent.setAction("com.jjj.action");
        //android5.0之后都要设置
        intent.setPackage("com.jjj.server");
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }


    private AshmemReader mAshmemReader;

    private void doAction() {
        try {

            mAshmemReader=new AshmemReader();
            int mWidth = mAshmem.getVideoWidth();
            int mHeight = mAshmem.getVideoHeight();

            //必须要ParcelFileDescriptor.fromFd(mAshmem.getFd()) 在这个作用域下创建一个fd的副本
            //否则单独靠int是无法权限访问使用的
            mAshmemReader.init(mAshmem.getParcelFileDescriptor(), mWidth, mHeight);

            while (isConnect) {
                Thread.sleep(32);
                byte[] d = mAshmemReader.readFrame();
                if (d == null) {
                    continue;
                }
                binding.renderView.setRenderData(d, mWidth, mHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                binding.de1.setText("ERR");
            });
        }
    }
}