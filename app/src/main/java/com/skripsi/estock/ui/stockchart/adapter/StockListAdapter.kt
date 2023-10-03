package com.skripsi.estock.ui.stockchart.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.estock.databinding.ItemStockBinding

import com.skripsi.estock.datasource.model.DetailCompany
import com.skripsi.estock.setSafeOnClickListener

internal class StockListAdapter (private val listStock: List<DetailCompany>) : RecyclerView.Adapter<StockListAdapter.StockListAdapters>() {

        inner class StockListAdapters(val binding: ItemStockBinding) :
            RecyclerView.ViewHolder(binding.root)

        var listener: StockClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockListAdapters {
            val binding =
                ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return StockListAdapters(binding)
        }

        override fun getItemCount(): Int {
            return listStock.size
        }

        override fun onBindViewHolder(holder: StockListAdapters, position: Int) {
            val stock = listStock[position]
            holder.binding.apply {
                tvNameStock.text = stock.name
                tvStockCode.text = stock.code

                Log.d("TAG_STOCK", "stock id :${stock.id}")

                cvStock.setSafeOnClickListener {
                    listener?.onCardDetailClicked(stock.id)
                }

            }
        }

        interface StockClickListener {
            fun onCardDetailClicked(id: String?)
        }
    }