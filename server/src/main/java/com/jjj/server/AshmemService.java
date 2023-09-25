package com.jjj.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Size;

import com.jjj.server.c.Camera2Helper;


public class AshmemService extends Service {

    private static final String TAG = "AshmemService";
    public static int mIndex = 0;
    private AshmemWriter mAshmemStub;
    private Camera2Helper mCameraHelp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCameraHelp == null) {
            initCameraHelp();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mCameraHelp == null) {
            initCameraHelp();
        }
        return mAshmemStub;
    }

    private void initCameraHelp() {
        mCameraHelp = new Camera2Helper(this,mIndex);
        mCameraHelp.setYUVDataCallback((y, uv, vu, width, height, stride) -> {
            mAshmemStub.refreshFrame(y, uv);
        });
        Size size = mCameraHelp.getConfig();
        mAshmemStub = AshmemWriter.getInstance();
        mAshmemStub.init(size.getWidth(), size.getHeight());

        if (!mCameraHelp.isStarting()) {
            mCameraHelp.start(null);
        }
    }


}
