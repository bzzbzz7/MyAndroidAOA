package com.yuneec.UsbService.adk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.zz.android.utils.LogUtils;
import com.yuneec.UsbService.adk.respone.AdkMessage;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class AdkManager implements IAdkManager {
    private static final String LOG_TAG = "AdkManager";
    private static final String ACTION_USB_PERMISSION = "com.android.utils.USB_PERMISSION";
    private static final int BUFFER_SIZE = 255;

    private Context mContext;
    private UsbManager mUsbManager;
    private UsbAccessory mUsbAccessory;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private PendingIntent mPermissionIntent;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private IntentFilter mDetachedFilter;
    private BroadcastReceiver mUsbReceiver;

    private int mByteRead = 0;

    public AdkManager(Context ctx) {
        // Store Android UsbManager reference
        mContext = ctx;
        this.mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        
        attachUsbReceiver();
        attachFilters();
    }

    private void attachFilters() {
        // Filter for detached events
        mDetachedFilter = new IntentFilter();
        mDetachedFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        mDetachedFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        mDetachedFilter.addAction(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbReceiver, mDetachedFilter);
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    private void attachUsbReceiver() {
        // Broadcast Receiver
        mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
                    UsbAccessory usbAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (usbAccessory != null && usbAccessory.equals(mUsbAccessory)) {
                        closeAccessory();
                    }
                }else if(action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (accessory != null) {
                            LogUtils.d("usb EXTRA_PERMISSION_GRANTED");
                            openAccessory(mUsbAccessory);
                        }
                    } else {
                        LogUtils.d("usb EXTRA_PERMISSION_GRANTED null!");
                        // set permision
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                    }                 
                }else if(action.equals(ACTION_USB_PERMISSION)){
                    synchronized (this) {
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (accessory != null) {
                                LogUtils.d("usb EXTRA_PERMISSION_GRANTED");      
                                openAccessory(mUsbAccessory);
                            }
                        } else {
                            LogUtils.d("usb EXTRA_PERMISSION_GRANTED null!");
                        }
                    }
                }
            }
        }; 
    }

    protected void openAccessory(UsbAccessory usbAccessory) {
        mParcelFileDescriptor = mUsbManager.openAccessory(usbAccessory);
        if (mParcelFileDescriptor != null) {
            mUsbAccessory = usbAccessory;
            FileDescriptor fileDescriptor = mParcelFileDescriptor.getFileDescriptor();

            if (fileDescriptor != null) {
                mFileInputStream = new FileInputStream(fileDescriptor);
                mFileOutputStream = new FileOutputStream(fileDescriptor);
            }
        }
    }

    protected void closeAccessory() {       
        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
            }
            if(mFileInputStream != null){
                mFileInputStream.close();
            }
            if(mFileOutputStream!=null){
                mFileOutputStream.close();
            }
        } catch (IOException e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }
        
        mParcelFileDescriptor = null;
        mUsbAccessory = null; 
        mContext.unregisterReceiver(mUsbReceiver);
    }

    public boolean serialAvailable() {
        return mByteRead >= 0;
    }

    @Override
    public void write(byte[] values) {
        if(mFileOutputStream == null){
            return;
        }
        try {
            mFileOutputStream.write(values);
        } catch (IOException e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void write(byte value) {
        write((int) value);
    }

    @Override
    public void write(int value) {
        if(mFileOutputStream == null){
            return;
        }
        try {
            mFileOutputStream.write(value);
        } catch (IOException e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void write(float value) {
        String bufferOfString = String.valueOf(value);
        write(bufferOfString);
    }

    @Override
    public void write(String text) {
        if(mFileOutputStream == null){
            return;
        }else{
            byte[] buffer = text.getBytes();

            try {
                mFileOutputStream.write(buffer);
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, e.getMessage());
            }
        } 
    }

    @Override
    public AdkMessage read() {
        if(mFileInputStream == null){
            return null;
        }else{
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] response;
            AdkMessage message = null;
            
            try {
                // Read from ADK
                mByteRead = mFileInputStream.read(buffer, 0, buffer.length);

                if (mByteRead != -1) {
                    // Create a new buffer that fits the exact number of read bytes
                    response = Arrays.copyOfRange(buffer, 0, mByteRead);

                    // Prepare a message instance
                    message = new AdkMessage(response);
                } else {
                    message = new AdkMessage(null);
                }
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, e.getMessage());
                message = null;
            }

            return message;
        }  
    }

    @Override
    public void close() {
        closeAccessory();
    }

    @Override
    public void open() {
        if (mFileInputStream == null || mFileOutputStream == null) {
            UsbAccessory[] usbAccessoryList = mUsbManager.getAccessoryList();
            if (usbAccessoryList != null && usbAccessoryList.length > 0) {
                openAccessory(usbAccessoryList[0]);
            }
        }
    }

    public IntentFilter getDetachedFilter() {
        return mDetachedFilter;
    }
    public BroadcastReceiver getUsbReceiver() {
        return mUsbReceiver;
    }

    // Protected methods used by tests
    // -------------------------------

    protected void setFileInputStream(FileInputStream fileInputStream) {
        this.mFileInputStream = fileInputStream;
    }

    protected void setFileOutputStream(FileOutputStream fileOutputStream) {
        this.mFileOutputStream = fileOutputStream;
    }

    protected UsbManager getUsbManager() {
        return mUsbManager;
    }
}
