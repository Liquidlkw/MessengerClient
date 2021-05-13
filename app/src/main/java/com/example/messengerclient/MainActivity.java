package com.example.messengerclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Messenger mService;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            content.setText("连接状态：connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            content.setText("连接状态：disconnected");
        }
    };
    private Button button;
    private TextView content;
    @SuppressLint("HandlerLeak")
    private Messenger mMessenger = new Messenger(new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what) {
                case 100:
                    Bundle data = msgFromServer.getData();
                    content.setText("服务器返回内容\n" + data.get("reply"));
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        content = findViewById(R.id.content);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MESSENGER");
        intent.setPackage("com.example.messengerservice");
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        button.setOnClickListener(v -> {
            Message msg = new Message();
            msg.what = 200;
            Bundle bundle = new Bundle();
            bundle.putString("msg", "我是client哦！！");
            msg.setData(bundle);
            //在Message中放入客户端的Messenger 服务端因此可以回传信息给客户端
            msg.replyTo = mMessenger;
            if (mService != null) {
                //已链接
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }
}