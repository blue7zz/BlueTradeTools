package com.example.mytradetools.websocket;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.example.mytradetools.BuildConfig;
import com.example.mytradetools.Config;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketService extends Service {
    private final static String TAG = WebSocketService.class.getSimpleName();

    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;
    private static final long CLOSE_RECON_TIME = 100;//连接断开或者连接错误立即重连

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        LogUtil.i(TAG, "WebSocketService onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化WebSocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测

        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //Android4.3以下 ，隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //Android4.3 - Android8.0，隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else {
            //Android8.0以上app启动后通知栏会出现一条"正在运行"的通知
//            NotificationChannel channel = new NotificationChannel(NotificationUtil.channel_id, NotificationUtil.channel_name,
//                    NotificationManager.IMPORTANCE_HIGH);
//            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//                Notification notification = new Notification.Builder(getApplicationContext(), NotificationUtil.channel_id_tai_bang).build();
//                startForeground(GRAY_SERVICE_ID, notification);
//            }
        }
        return START_STICKY;
    }

    private void initSocketClient() {
//        String url = BuildConfig.WS_PERFIX;
        URI uri = URI.create(Config.INSTANCE.getWssUri());
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                //message就是接收到的消息
//                LogUtil.i(TAG, "WebSocketService收到的消息：" + message);
//                EventBus.getDefault().post(new WebSocketEvent(message));
            }

            @Override
            public void onOpen(ServerHandshake handShakeData) {//在webSocket连接开启时调用
//                LogUtil.i(TAG, "WebSocket 连接成功");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {//在连接断开时调用
//                LogUtil.e(TAG, "onClose() 连接断开_reason：" + reason);

                mHandler.removeCallbacks(heartBeatRunnable);
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);//开启心跳检测
            }

            @Override
            public void onError(Exception ex) {//在连接出错时调用
//                LogUtil.e(TAG, "onError() 连接出错：" + ex.getMessage());

                mHandler.removeCallbacks(heartBeatRunnable);
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);//开启心跳检测
            }
        };
        connect();
    }

    /**
     * 连接WebSocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 发送消息
     */
    public void sendMsg(String msg) {
        if (null != client) {
//            LogUtil.i(TAG, "发送的消息：" + msg);
            try {
                client.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        LogUtil.e(TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

    /**
     * 断开连接
     */
    public void closeConnect() {
        mHandler.removeCallbacks(heartBeatRunnable);
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    //    -------------------------------------WebSocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
//                    LogUtil.e(TAG, "心跳包检测WebSocket连接状态：已关闭");
                } else if (client.isOpen()) {
//                    LogUtil.d(TAG, "心跳包检测WebSocket连接状态：已连接");
                } else {
//                    LogUtil.e(TAG, "心跳包检测WebSocket连接状态：已断开");
                }
            } else {
                //如果client已为空，重新初始化连接
                initSocketClient();
//                LogUtil.e(TAG, "心跳包检测WebSocket连接状态：client已为空，重新初始化连接");
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
//                    LogUtil.e(TAG, "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

//4、在Application中开启WebSocketService服务
//
//    /**
//     * 开启并绑定WebSocket服务
//     */
//    public void startWebSocketService() {
//        Intent bindIntent = new Intent(this, WebSocketService.class);
//        startService(bindIntent);
//        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
//    }
//
//作者：BillyZuo
//链接：https://www.jianshu.com/p/6ff879706d9c
//来源：简书
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。