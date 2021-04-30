package com.example.mytradetools

object Config {

    val wssUri: String = "wss://wspri.coinall.ltd:8443/ws/v5/public"

    //这里是商品代码,必须要有对应的交割合约和永续合约
    val symbolList: ArrayList<String> = arrayListOf(
        "BTC",
        "ETH",
        "LTC",
        "DOT",
        "FIL",
        "XRP",
        "TRX",
        "ADA",
        "BCH",
        "BSV",
        "EOS",
        "ETC",
        "LINK"
    )

    const val sustainableCode = "SWAP";//永续代号
    const val nextQuarterCode = "210924";//次季代号


}