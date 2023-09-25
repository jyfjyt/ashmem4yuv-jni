package com.jjj.client;


import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class AshmemReader {

    static {
        System.loadLibrary("ashmem-sdk");
    }

    private long mAshmemId;
    private int mSize;

    private byte[] mBuffer;

    public void init(ParcelFileDescriptor pfd, int width, int height) {

        mSize = width * height * 3 / 2;
        int sharedFd=pfd.getFd();

        mAshmemId = initAshmemByFd(sharedFd, mSize);

        Log.i("123123", "client sharedFd:" + sharedFd);
        Log.i("123123", "client mSize:" + mSize);
        Log.i("123123", "client mAshmemId:" + mAshmemId);

        if (mBuffer ==null){
            mBuffer =new byte[mSize];
        }
    }

    public byte[] readFrame() {
        if (mAshmemId <= 0) {
            return null;
        }
        readAshmemData(mAshmemId, 0, mSize, mBuffer);

        return mBuffer;
    }

    public native long initAshmemByFd(int fd, int size);

    public native void readAshmemData(long ashmemHandler, int offset, int lenght,byte[] buf);


}
