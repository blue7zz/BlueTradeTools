package com.example.mytradetools.bean


/**
 * {"op":"subscribe","args":[{"channel":"tickers","instId":"BTC-USDT-SWAP"}]}
 */
class SubscribeMsg(
    val args: List<Arg>,
    val op: String
) {
    //内部类
    class Arg() {

        var channel: String? = null
        var instId: String? = null

    }
}



