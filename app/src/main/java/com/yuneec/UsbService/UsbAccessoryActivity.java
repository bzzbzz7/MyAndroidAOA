package com.yuneec.UsbService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.yuneec.UsbService.adk.AdkUtils;
import com.zz.android.utils.TextViewUtils;

public class UsbAccessoryActivity extends Activity {

    TextView tv_debugText;
    LocalBroadcastManager mLocalBroadcastManager;    
    BroadcastReceiver mReceiver;    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_accessory);
        tv_debugText = (TextView)findViewById(R.id.tv_debugText);

        AdkUtils.getInstance().start(getApplicationContext());

        mReceiver = new BroadcastReceiver() {    
            @Override    
            public void onReceive(Context context, Intent intent) {    
                if (intent.getAction().equals(AdkUtils.ACTION_USB_MESSAGE)) {    
                    //Do Something  
                    TextViewUtils.append(tv_debugText, new String(intent.getByteArrayExtra(AdkUtils.ACTION_USB_MESSAGE)));
                }   
            }    
        };

        IntentFilter filter = new IntentFilter();    
        filter.addAction(AdkUtils.ACTION_USB_MESSAGE);    
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);  
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdkUtils.getInstance().close();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);  
    }
}
