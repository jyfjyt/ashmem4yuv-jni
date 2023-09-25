// IAshmem.aidl
package com.jjj.server;


interface IAshmem {

     int getVideoWidth();

     int getVideoHeight();

     ParcelFileDescriptor getParcelFileDescriptor();
}