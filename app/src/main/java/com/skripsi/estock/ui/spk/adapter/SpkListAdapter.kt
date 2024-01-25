package com.skripsi.estock.ui.spk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.estock.databinding.ItemSpkBinding
import com.skripsi.estock.datasource.model.DetailCompany

internal class SpkListAdapter (private val listStock: List<DetailCompany>) : RecyclerView.Adapter<SpkListAdapter.SpkListAdapters>() {

    inner class SpkListAdapters(val binding: ItemSpkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpkListAdapters {
        val binding =
            ItemSpkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpkListAdapters(binding)
    }

    override fun getItemCount(): Int {
        return listStock.size
    }

    override fun onBindViewHolder(holder: SpkListAdapters, position: Int) {
        if (position < listStock.size) {
            val stock = listStock[position]
            holder.binding.apply {
                tvNameStock.text = stock.name ?: "Name Not Available"
                tvStockCode.text = stock.code ?: "Code Not Available"
                tvScore.text = String.format("%.2f", stock.spk_score ?: 0.0)

                Log.d("TAG_STOCK", "stock id :${stock.id}")
                Log.d("TAG_STOCK_Score", "score :${stock.spk_score}")
            }
        } else {
            Log.e("TAG_posi", "Invalid position: $position, list size: ${listStock.size}")
        }
    }


}