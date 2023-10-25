package com.skripsi.estock.ui.homepage.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.estock.databinding.ItemTopBinding
import com.skripsi.estock.datasource.model.DetailCompany
import com.skripsi.estock.setSafeOnClickListener

internal class HomeAdapter (private val listStock: List<DetailCompany>) : RecyclerView.Adapter<HomeAdapter.HomeAdapters>() {

    inner class HomeAdapters(val binding: ItemTopBinding) :
        RecyclerView.ViewHolder(binding.root)

    var listener: HomeClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapters {
        val binding =
            ItemTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeAdapters(binding)
    }

    override fun getItemCount(): Int {
        return listStock.size
    }

    override fun onBindViewHolder(holder: HomeAdapters, position: Int) {
        val stock = listStock[position]
        holder.binding.apply {
            tvNameStock.text = stock.name
            tvScore.text = stock.spk_score.toString()

            Log.d("TAG_STOCK", "stock id :${stock.id}")
            Log.d("TAG_STOCK_Score", "score :${stock.spk_score}")

            cvSpk.setSafeOnClickListener {
                listener?.onCardDetailClicked(stock.id)
            }

        }
    }

    interface HomeClickListener {
        fun onCardDetailClicked(id: String?)
    }
}