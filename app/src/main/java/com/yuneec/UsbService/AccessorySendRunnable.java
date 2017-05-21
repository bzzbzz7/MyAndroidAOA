package com.yuneec.UsbService;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccessorySendRunnable implements Runnable{
    UsbManager mUsbManager;
    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    
    byte[] bytes;

    
    public AccessorySendRunnable(UsbManager mUsbManager, UsbAccessory mAccessory, byte[] bytes){
        this.mUsbManager = mUsbManager;
        this.mAccessory = mAccessory;
        this.bytes = bytes;  
    }
    public void run() {
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mOutputStream = new FileOutputStream(fd);

            try {
                mOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        }
    }
}
