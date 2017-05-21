package com.yuneec.UsbService;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zz.android.utils.TextViewUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class AdbServerActivity extends Activity implements OnClickListener {
    TextView tv_debugText;
    Button bt_hello;
    
    int SOCKET_PORT = 6000; 
    ServerSocket serverSocket = null;
    Socket client = null;
    boolean isLoop=true;
    Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_server);
        
        tv_debugText = (TextView)findViewById(R.id.tv_debugText);
        bt_hello = (Button)findViewById(R.id.bt_hello);
        bt_hello.setOnClickListener(this);
        
        handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {                 
                    TextViewUtils.append(tv_debugText, (String)msg.obj);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(SOCKET_PORT);
                    handlerDebugText("server started-----");
                    
                    while(isLoop){
                        Socket socket = serverSocket.accept();  
                        if(client !=null){
                            client.close();
                        }
                        client = socket;
                        client.setKeepAlive(true);
                        client.setReuseAddress(true);
                        client.setSoTimeout(0);
                        handlerDebugText("connected.... local addr:" + client.getLocalSocketAddress() + " remote addr:" + client.getRemoteSocketAddress());
                    }
                } catch (IOException e) {
                    handlerDebugText(e.getMessage());
                }
            }
        }).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isLoop){
                    receiveMessage(client);
                }      
            }
        }).start();
        
        
    }
    
    @Override
    public void onClick(android.view.View v) {
        switch(v.getId()){
            case R.id.bt_hello:
                sendMessage(client, "hello");
                break;
            default:
                break;        
        }
    };
    
    private void sendMessage(Socket client, String str) {
        if(client!=null && !client.isClosed()){
            try {
                Writer writer = new OutputStreamWriter(client.getOutputStream());
                writer.write(str);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void receiveMessage(Socket client) {
        if(client!=null && !client.isClosed()){
            try {
                Reader reader = new InputStreamReader(client.getInputStream());
                
                char[] buffer = new char[256];   
                int len = 0;
                while((len=reader.read(buffer))!=-1){    
//                    if(len == 0){   //receive 0 byte means socket is closed
//                        client.close();
//                        break;
//                    }else{
                        handlerDebugText("receive:" + new String(buffer,0,len));   
//                    }    
                }
                reader.close();
            } catch (IOException e) {
                handlerDebugText("receive:" + e.getMessage());
            }
        }
    }
    
    private void handlerDebugText(String str) {
        Message msg = handler.obtainMessage(0);
        msg.obj = str;
        if (msg.getTarget() != null) {
            msg.sendToTarget();
        }
    }  
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLoop = false;
        if(client!=null)
        {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    
    
}
