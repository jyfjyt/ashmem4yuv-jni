package com.jjj.server;

import android.os.Build;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

        int sharedFd=-1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android version >= 8.0
            try {
                MemoryFile memoryFile = new MemoryFile(ASHM_FILE_NAME, getSize());
                Class<?> memoryFileClass = Class.forName("android.os.MemoryFile");
                Method getFileDescriptorMethod = memoryFileClass.getDeclaredMethod("getFileDescriptor");
                getFileDescriptorMethod.setAccessible(true);
                FileDescriptor fd = (FileDescriptor) getFileDescriptorMethod.invoke(memoryFile);
                mParcelFileDescriptor=ParcelFileDescriptor.dup(fd);
                mSharedFd = mParcelFileDescriptor.getFd();
            } catch (IOException | IllegalAccessException |
                     ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            //Android version < 8.0 没权限用jni的open方法直接对 /dev/ashmem虚拟文件进行操作
            mSharedFd = createAshmem(ASHM_FILE_NAME, getSize());
        }


        if (mSharedFd <= 0) {
            throw new RuntimeException("mSharedFd <= 0");
        }


        mAshmemId = initAshmemByFd(mSharedFd, getSize());

        if (mAshmemId <= 0) {
            throw new RuntimeException("mAshmemId <= 0");
        }
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
            //防止ParcelFileDescriptor被占用，所以要生成一个副本返回
            //确保文件描述符在跨进程通信中的正确性和安全性
            //当你从 Parcel 或 Bundle 中读取文件描述符时，它会在第一次读取后自动关闭，因此第二次读取时会返回 null。
            // 这是为了防止文件描述符被多个进程持有，从而导致资源泄漏或不安全的情况。
            return mParcelFileDescriptor.dup();
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
