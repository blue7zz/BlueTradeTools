package com.example.mytradetools.bean

/**
 * {"op":"subscribe","args":[{"channel":"tickers","instId":"BTC-USDT-SWAP"},{"channel":"tickers","instId":"ETH-USDT-SWAP"}]}
 */
class TickersMessage() {
    var args: ArrayList<Arg>? = null
    var op: String? = null
}

