package com.example.mytradetools.websocket

import android.util.Log
import com.example.mytradetools.bean.BasicBean
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.net.ssl.SSLParameters

open class JWebSocketClient(serverUri: URI?) : WebSocketClient(serverUri, Draft_6455()) {
    var mGson: Gson? = null

    //需要做成订阅管理
    val subscriptionMap: HashMap<String, ArrayList<Subscription>> = HashMap()

    override fun onSetSSLParameters(sslParameters: SSLParameters) {
//        super.onSetSSLParameters(sslParameters);
    }

    override fun onOpen(handShakeData: ServerHandshake) { //在webSocket连接开启时调用
        mGson = Gson()

    }

    override fun onMessage(message: String) { //接收到消息时调用
        Log.e("message", message)
        //消息解析，分发到消息处理中心
        //等待客户端内部订阅
        var bean: BasicBean? = mGson?.fromJson(message, BasicBean::class.java)
        var subscriptionList = subscriptionMap[bean?.arg?.channel]
        if (subscriptionList != null) {
            for (subscription in subscriptionList) {
                //发送到订阅上面
                subscription.callback(message)
            }
        }
    }


    override fun onClose(code: Int, reason: String, remote: Boolean) { //在连接断开时调用
        Log.e("onOpen", "连接关闭")
    }

    override fun onError(ex: Exception) { //在连接出错时调用
    }


    /**
     * 订阅单独一个频道
     */
    fun subscription(channel: String, subscription: Subscription) {
        if (subscriptionMap[channel] == null) {
            var list: ArrayList<Subscription> = ArrayList()
            list.add(subscription);
            subscriptionMap[channel] = list
        } else {
            var list = subscriptionMap[channel];
            list?.add(subscription);
            list?.let { subscriptionMap.put(channel, it) }
        }
    }
}

/**
 * 订阅消息
 */
interface Subscription {
    fun callback(msg: String)
}
