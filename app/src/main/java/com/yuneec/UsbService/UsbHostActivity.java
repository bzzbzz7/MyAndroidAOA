package com.yuneec.UsbService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class UsbHostActivity extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.yuneec.UsbService.USB_PERMISSION";
    TextView tv_debugText;
    UsbManager mUsbManager;
    PendingIntent mPermissionIntent;
    boolean isRequestingPermission = false;
    Handler mainHandler = new Handler();
    HandlerThread sendThread = null;
    Handler sendHandler = null;
    public final BroadcastReceiver mUsbReceiver = new AOAReceiver() ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_accessory);
        tv_debugText = (TextView)findViewById(R.id.tv_debugText);

        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, usbFilter);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter permissionFilter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, permissionFilter);

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        addDebugText("initial");
        sendThread = new HandlerThread("sendThread");
        sendThread.start();
        sendHandler = new Handler(sendThread.getLooper());

        //手动启动APP后，判断AOA是否连接上，连接上的则弹框请求USB权限，
        if(mUsbManager.getAccessoryList() != null && mUsbManager.getAccessoryList().length>0){
            Intent intent = getIntent();
            //final UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);  //unusefull 手动启动APP，Intent里面没有设置EXTRA_ACCESSORY
            final UsbAccessory accessory = mUsbManager.getAccessoryList()[0];
            if (mUsbManager.hasPermission(accessory)) {
                if (accessory != null) {
                    addDebugText("usb EXTRA_PERMISSION_GRANTED");
                }
            } else {
                addDebugText("usb EXTRA_PERMISSION_GRANTED null!!!");

                // set permision
                synchronized (this) {
                    mUsbManager.requestPermission(accessory, mPermissionIntent);
                }
            }
        }

    }

    //APP正在运行，插拔AOA，获取到系统发送的Intent（包含ACCESSORY_ATTACHED信息）
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
            addDebugText("ACTION_USB_ACCESSORY_ATTACHED");
            final UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (accessory != null) {
                    addDebugText("usb EXTRA_PERMISSION_GRANTED");
                }
            } else {
                addDebugText("usb EXTRA_PERMISSION_GRANTED null!!!");

                // set permision
                synchronized (this) {
                    mUsbManager.requestPermission(accessory, mPermissionIntent);
                }
            }
        }
    }

    public class AOAReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_USB_PERMISSION:
                    addDebugText("ACTION_USB_PERMISSION");

                    synchronized (this) {
                        final UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (accessory != null) {
                                addDebugText("usb EXTRA_PERMISSION_GRANTED");
                            }
                        } else {
                            addDebugText("usb EXTRA_PERMISSION_GRANTED null!!!");
                            // set permision
                            synchronized (this) {
                                mUsbManager.requestPermission(accessory, mPermissionIntent);
                            }
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                    addDebugText("fuck you");

                    break;
                case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    addDebugText("ACTION_USB_ACCESSORY_DETACHED");
                    break;
            }
        }
    }

    
    private void sendToUsb(UsbDevice device, byte[] bytes) {
        sendHandler.post(new HostSendRunnable(mUsbManager, device, bytes));
    }
    Thread receiveThread = new Thread(new Runnable() {
        @Override
        public void run() {}
    });
    
    @SuppressLint("SimpleDateFormat")
    private void addDebugText(String msg) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        tv_debugText.append(date + " " + msg + "\n");
        int texLen = tv_debugText.getText().length();
        if (texLen > 2000) {
            tv_debugText.setText(tv_debugText.getText().subSequence(texLen - 2000, texLen));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        sendThread.quit();
    }
}
