package com.yuneec.UsbService;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class HostSendRunnable implements Runnable{
    UsbManager mUsbManager;
    UsbDevice device;
    byte[] bytes;
    int TIMEOUT = 0;
    boolean forceClaim = true;
    
    public HostSendRunnable(UsbManager mUsbManager, UsbDevice device, byte[] bytes){
        this.mUsbManager = mUsbManager;
        this.device = device;
        this.bytes = bytes;
        
    }
    public void run() {
        UsbInterface intf = device.getInterface(0);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(device); 
        connection.claimInterface(intf, forceClaim);
        connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT); //do in another thread
    }
}
