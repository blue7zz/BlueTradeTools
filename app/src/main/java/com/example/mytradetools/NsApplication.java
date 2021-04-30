package com.example.mytradetools;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.example.mytradetools.websocket.WebSocketService;

import org.json.JSONException;
import org.json.JSONObject;

public class NsApplication extends Application {
    private final static String TAG = NsApplication.class.getSimpleName();

    private static NsApplication instance;
    private static final String DEVICE_TOKEN = "device_token";//设备token
    public WebSocketService mWebSocketService;
    //    -------------------------------------WebSocket发送空消息心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 60 * 1000;//每隔1分钟发送空消息保持WebSocket长连接

    public static NsApplication getInstance() {
        if (instance == null) {
            instance = new NsApplication();
        }
        return instance;
    }

    private Handler mHandler = new Handler();

    private Runnable webSocketRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWebSocketService != null && mWebSocketService.client != null && mWebSocketService.client.isOpen()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("from", "");
                    jsonObject.put("to", "");
                    mWebSocketService.sendMsg(jsonObject.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };


    public WebSocketService getWebSocketService() {
        return mWebSocketService;
    }

    /**
     * 开启并绑定WebSocket服务
     */
    public void startWebSocketService(String deviceToken) {
        Intent bindIntent = new Intent(this, WebSocketService.class);
        bindIntent.putExtra(DEVICE_TOKEN, deviceToken);
        startService(bindIntent);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        mHandler.removeCallbacks(webSocketRunnable);
        mHandler.postDelayed(webSocketRunnable, HEART_BEAT_RATE);//开启心跳检测
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            mWebSocketService = ((WebSocketService.JWebSocketClientBinder) iBinder).getService();
//            LogUtil.e(TAG, "WebSocket服务与Application成功绑定");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            mWebSocketService = null;
//            LogUtil.e(TAG, "WebSocket服务与Application成功断开");
        }
    };
}