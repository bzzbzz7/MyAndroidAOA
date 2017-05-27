package com.yuneec.UsbService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener{
    Button bt_exit;
    Button bt_usbAccessory;
    Button bt_usbHost;
    Button bt_adbServer;
    ImageView iv_bg;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_exit = (Button)findViewById(R.id.bt_exit);
        bt_usbAccessory = (Button)findViewById(R.id.bt_usbAccessory);
        bt_usbHost = (Button)findViewById(R.id.bt_usbHost);
        bt_adbServer = (Button)findViewById(R.id.bt_adbServer);
        bt_exit.setOnClickListener(this);
        bt_usbAccessory.setOnClickListener(this);
        bt_usbHost.setOnClickListener(this);
        bt_adbServer.setOnClickListener(this);
        iv_bg = (ImageView)findViewById(R.id.iv_bg);
        //Picasso.with(this).load("http://pic2016.5442.com:82/2016/0810/7/1.jpg").into(iv_bg);
        
    }
    
    @Override
    public void onClick(android.view.View v) {
        switch(v.getId()){
            case R.id.bt_exit:
                finish();
                break;
            case R.id.bt_usbAccessory:
                startActivity(new Intent(MainActivity.this,UsbAccessoryActivity.class));
                break;
            case R.id.bt_usbHost:
                startActivity(new Intent(MainActivity.this,UsbHostActivity.class));
                break;
            case R.id.bt_adbServer:
                startActivity(new Intent(MainActivity.this,AdbServerActivity.class));
                break;
            default:
                break;        
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
