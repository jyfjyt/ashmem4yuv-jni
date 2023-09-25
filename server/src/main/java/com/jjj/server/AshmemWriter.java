package com.jjj.server;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

public class AshmemWriter extends IAshmem.Stub {

    private static final String ASHM_FILE_NAME = "video_memory1";

    private static final AshmemWriter mInstance = new AshmemWriter();

    private int mWidth;
    private int mHeight;

    private int mSharedFd;
    private long mAshmemId;

    private ParcelFileDescriptor mParcelFileDescriptor;

    private AshmemWriter() {

    }

    public static AshmemWriter getInstance() {
        return mInstance;
    }


    public void init(int width, int height) {
        mWidth = width;
        mHeight = height;
        initAshmem();
        Log.i("123123", "server mFd:" + mSharedFd);
        Log.i("123123", "server mSize:" + getSize());
        Log.i("123123", "server mAshmemId:" + mAshmemId);
    }

    private void initAshmem() {
        mSharedFd= createAshmem(ASHM_FILE_NAME, getSize());
//        try {
//            mParcelFileDescriptor=ParcelFileDescriptor.fromFd(mSharedFd);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        mAshmemId = initAshmemByFd(mSharedFd, getSize());
    }


    private int getSize() {
        return mWidth * mHeight * 3 / 2;
    }

    @Override
    public int getVideoWidth() throws RemoteException {
        return mWidth;
    }

    @Override
    public int getVideoHeight() throws RemoteException {
        return mHeight;
    }

    @Override
    public ParcelFileDescriptor getParcelFileDescriptor() throws RemoteException {
        try {
            ParcelFileDescriptor p=ParcelFileDescriptor.fromFd(mSharedFd);
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void refreshFrame(byte[] y, byte[] uv) {
        writeAshmemData(mAshmemId, 0, y, y.length);
        writeAshmemData(mAshmemId, y.length, uv, uv.length);
    }

    public native int createAshmem(String ashmFileName, int size);

    public native long initAshmemByFd(int fd, int size);

    public native int writeAshmemData(long ashmemHandler, int offset, byte[] data, int lenght);

//    public native void clearHandler(long ashmemHandler,int size);


}
