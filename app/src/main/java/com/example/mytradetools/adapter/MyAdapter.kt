package com.example.mytradetools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mytradetools.R

class MyAdapter(
    private val dataSet: ArrayList<TickersDatas>,
) :
    RecyclerView.Adapter<MyAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        if (dataSet[position].name != null) {
            holder.tvTitle.text =
                dataSet[position].name
        }

        if (dataSet[position].sustainableTickersData != null) {
            holder.tv1.text =
                dataSet[position].name + "永续：" + dataSet[position].sustainableTickersData?.last + "$"
        }

        if (dataSet[position].nextQuarterTickersData != null) {
            holder.tv2.text =
                dataSet[position].name + "次季：" + dataSet[position].nextQuarterTickersData?.last + "$"
        }

        if (dataSet[position].sustainableTickersData?.last != null && dataSet[position].nextQuarterTickersData?.last != null) {
            var a = dataSet[position].sustainableTickersData?.last?.toFloat()
            var b = dataSet[position].nextQuarterTickersData?.last?.toFloat()
            holder.tv3.text = "差价：" + a?.let { b?.minus(it) }.toString() + "$"
            holder.tv4.text = "差价率：" + a?.let { b?.div(it) }.toString() + "%"

        }


    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    /**
     * 内部类
     */
    inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        //        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(v.context), v, true)
        val tv1: TextView = v.findViewById(R.id.textView)    //永续价格
        val tv2: TextView = v.findViewById(R.id.textView2)   //次季度
        val tv3: TextView = v.findViewById(R.id.textView3)   //次季度-永续差价
        val tv4: TextView = v.findViewById(R.id.textView4)   //次季度-永续差价
        val tvTitle: TextView = v.findViewById(R.id.tv_title)   //次季度-永续差价
    }


}