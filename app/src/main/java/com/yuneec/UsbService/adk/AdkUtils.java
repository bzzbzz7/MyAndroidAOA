package com.yuneec.UsbService.adk;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.yuneec.UsbService.adk.respone.AdkMessage;


public class AdkUtils {
    public static final String ACTION_USB_MESSAGE="ACTION_USB_MESSAGE";

    private AdkManager adkManager;
    private boolean isLoop = true;
    
    private static AdkUtils instance = null;
    public static AdkUtils getInstance() {
        if (instance == null) {
            instance = new AdkUtils();
        }
        return instance;
    }
    
    private AdkUtils(){       
    }
    
    public void start(final Context ctx){  
        if(adkManager!=null){
            return;
        }
        adkManager = new AdkManager(ctx);
        adkManager.open();
        isLoop=true;
        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isLoop){
                    AdkMessage adkMessage = adkManager.read();
                    if(adkMessage!=null){
                        LocalBroadcastManager.getInstance(ctx).sendBroadcastSync(new Intent(ACTION_USB_MESSAGE).putExtra(ACTION_USB_MESSAGE, adkMessage.getBytes()));                 
                    }else{
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        });
        receiveThread.start();
    }
    
    public void send(byte[] bytes){
        adkManager.write(bytes);
    }
    public void send(String str){
        adkManager.write(str);
    }
    
    public void close(){
        if(adkManager!=null){
            adkManager.close();
            adkManager=null;
        }
        isLoop = false;
    }
}
