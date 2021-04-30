package com.example.mytradetools.adapter

import com.example.mytradetools.bean.TickersData

/**
 * 二个TickerData
 *
 * 永续和次季度
 *
 */
class TickersDatas : Comparable<Any> {
    //名称 BTC
    var name: String = ""

    var data1: TickersData? = null //现货
    var data2: TickersData? = null //需要对比的币种

    //永续
    var sustainableTickersData: TickersData? = null

    //当周
    var inTheWeekTickersData: TickersData? = null

    //次周
    var nextWeekTickersData: TickersData? = null

    //当季
    var inTheQuarterTickersData: TickersData? = null

    //次季
    var nextQuarterTickersData: TickersData? = null


    override fun compareTo(other: Any): Int {
        var a = sustainableTickersData?.last?.toFloat()
        var b = nextQuarterTickersData?.last?.toFloat()
        var i = a?.let { b?.div(it) }
        return (i?.times(10000))?.toInt()!!
    }


}