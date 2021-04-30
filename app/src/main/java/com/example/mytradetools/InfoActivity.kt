package com.example.mytradetools

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytradetools.adapter.MyAdapter
import com.example.mytradetools.adapter.TickersDatas
import com.example.mytradetools.bean.Arg
import com.example.mytradetools.bean.TickersBean
import com.example.mytradetools.bean.TickersMessage
import com.example.mytradetools.databinding.ActivityInfoBinding
import com.example.mytradetools.databinding.ActivityMainBinding
import com.example.mytradetools.websocket.JWebSocketClient
import com.google.gson.Gson
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * 移到Service里面
 * 自动重连
 *
 */
class InfoActivity : AppCompatActivity() {
    var gson: Gson = Gson()
    var mClient: JWebSocketClient? = null
    var binding: ActivityInfoBinding? = null    //自动生成
    val tickersDataList: ArrayList<TickersDatas> = ArrayList() //数据
    var myAdapter: MyAdapter? = null;
    var mHandler: Handler? = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    fun initSocket() {
        var uri: URI = URI.create(Config.wssUri)
        mClient = object : JWebSocketClient(uri) {
            override fun onMessage(message: String) {
                super.onMessage(message)
                messageDealWith(message)
                sort()
            }

            override fun onOpen(handShakeData: ServerHandshake) {
                super.onOpen(handShakeData)
                mHandler?.post { binding?.connectBtn?.text = "连接成功" }

            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                super.onClose(code, reason, remote)
                mHandler?.post { binding?.connectBtn?.text = "连接" }
            }
        }
    }

    /**
     * 排序，以差价率进行排序，差价最高的放在最前面
     */
    fun sort() {
//        tickersDataList.sortBy { it }
//        mHandler?.post { myAdapter?.notifyDataSetChanged() }
    }

    /**
     * 处理归纳信息，并且填充到对应位置
     * 数据结构
     * name:btc
     * sustainableTickersData = XXX-USDT-SWAP
     * inTheWeekTickersData = BTC-USD-191115  191115 解析成时间 当天时间+7 <=191115 当周
     * nextWeekTickersData = BTC-USD-191115  191115 解析成时间 当天时间+14 <=191115 次周
     * inTheQuarterTickersData = BTC-USD-191115  191115 解析成时间 当天时间+90 <=191115 当季
     * nextQuarterTickersData = BTC-USD-191115  191115 解析成时间 当天时间+180 <=191115 次季度
     *
     */
    fun messageDealWith(message: String) {
        //解析数据，目前只有单独一种类型
        //先遍历数据集合，判断有没有这个数据集合
        //进行归纳并且更新，每个instID，只能存储一个
        //instId:"BTC-USDT"
        //        BTC-USD-191115
        //并且每一个类型归纳为一个里面，目前来说
        val data: TickersBean = gson.fromJson(message, TickersBean::class.java)
        if (data.data?.size!! > 0) {
            val s = data.data!![0].instId.split("-")
            //找已存在的
            var isHave = false
            tickersDataList.forEachIndexed { i, item ->
                if (item.name == s[0]) {
                    isHave = true
                    //找到对应数据，开始判断类型塞入
                    //代码先写死，先实现，后面配置
                    when {
                        data.data!![0].instId.contains(tickers1) -> {
                            item.data1 = data.data!![0]
                        }
                        data.data!![0].instId.contains(tickers2) -> {
                            item.data2 = data.data!![0]
                        }
                    }
                    mHandler?.postDelayed({ myAdapter?.notifyItemChanged(i) }, 50)
                }
            }

            if (!isHave) {
                val tickersDatas = TickersDatas()
                tickersDatas.name = s[0]
                when {
                    data.data!![0].instId.contains(tickers1) -> {
                        tickersDatas.data1 = data.data!![0]
                    }
                    data.data!![0].instId.contains(tickers2) -> {
                        tickersDatas.data2 = data.data!![0]
                    }
                }
                tickersDataList.add(tickersDatas)
                mHandler?.postDelayed(
                    { myAdapter?.notifyItemChanged(tickersDataList.size - 1) },
                    50
                )
            }
        }


    }

    /**
     * 测试数据
     */
    fun testData() {

    }

    fun initView() {
        testData()
        myAdapter = MyAdapter(tickersDataList)
        binding?.recyclerview?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerview?.adapter = myAdapter

        binding?.connectBtn?.setOnClickListener {
            mHandler?.post { binding?.connectBtn?.text = "连接中" }

            mClient?.connect()
        }
        binding?.subscribeMsg?.setOnClickListener {
            //订阅
            var tickersMessage = TickersMessage();
            tickersMessage.op = "subscribe"
            tickersMessage.args = ArrayList()
            for (s in Config.symbolList) {
                tickersMessage.args?.add(Arg("tickers", s + tickers1))
                tickersMessage.args?.add(Arg("tickers", s + tickers2))
            }

            mClient?.send(gson.toJson(tickersMessage))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mClient?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mClient = null
        }
    }

    var tickers1: String = "-USDT"
    var tickers2: String = "-USDT-210924"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_info);
        initSocket()
        initView()
        //需要传入2个参数，对比1 和对比2
        //比如BTC-USDT-SWAP    //前面的BTC 不用传输因为会遍历所有的配置表，后面的需要
        //比如BTC-USDT-210924
        //BTC-USDT
    }


    companion object {
        /**
         * 定义一个特殊的启动
         */
        fun StartActivity(context: Context, tickers1: String, tickers2: String) {
            val intent = Intent(context, InfoActivity::class.java)
            intent.putExtra("tickers1", tickers1)
            intent.putExtra("tickers2", tickers2)
            context.startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}