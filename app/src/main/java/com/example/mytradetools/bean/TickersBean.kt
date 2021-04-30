package com.example.mytradetools.bean

/**
 * Tick
 */
class TickersBean : BasicBean() {
    var data: List<TickersData>? = null
}


/**
 * 行情频道数据
 */
data class TickersData(
    var askPx: String,          //卖一价
    var askSz: String,          //卖一价对应的量
    var bidPx: String,          //买一价
    var bidSz: String,          //买一价对应的量
    var high24h: String,        //24小时最高价
    var instId: String,         //产品ID
    var instType: String,       //产品类型
    var last: String,           //最新成交价
    var lastSz: String,         //最新成交的数量
    var low24h: String,         //24小时最低价
    var open24h: String,        //24小时开盘价
    var sodUtc0: String,        //UTC-0时开盘价
    var sodUtc8: String,        //UTC-8时开盘价
    var ts: String,             //数据产生时间
    var vol24h: String,         //24小时成交量 以涨为单位
    var volCcy24h: String       //24小时程佳亮以币为单位
)


