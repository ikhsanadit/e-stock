package com.skripsi.estock.ui.stockchart

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityStockChartBinding
import com.skripsi.estock.datasource.model.*
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.addstock.AddStockActivity
import com.skripsi.estock.ui.detailstock.DetailStockActivity
import com.skripsi.estock.ui.stockchart.adapter.StockListAdapter


class StockChartActivity : AppCompatActivity(), StockListAdapter.StockClickListener {
    private lateinit var binding: ActivityStockChartBinding
    private lateinit var stockAdapter: StockListAdapter
    private lateinit var progresDialog: ProgressDialog

    private var list: MutableList<DetailCompany> = mutableListOf()

    private val firestoreDb = Firebase.firestore

    private val stockIdMap: MutableMap<String, String> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        progresDialog = ProgressDialog(this)
        progresDialog.setMessage("Mengambil data")

        stockAdapter = StockListAdapter(list)
        stockAdapter.listener = this

        binding.rvListStock.layoutManager = LinearLayoutManager(this)
        binding.rvListStock.adapter = stockAdapter

        binding.apply {
            btnAdd.setSafeOnClickListener {
                startActivity(Intent(this@StockChartActivity, AddStockActivity::class.java))
                finish()
            }
        }

        stockAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()

        getData()
    }


    private fun getData() {
        progresDialog.show()
        firestoreDb.collection("detail company")
            .get()
            .addOnCompleteListener { task ->
                Log.d("TAG task", "isi task: $task ")
                list.clear()
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val stockId = document.id
                        val name = document.getString("name")
                        val code = document.getString("code")
                        val gpmMap = document.get("gpm") as? Map<*, *>
                        val npmMap = document.get("npm") as? Map<*, *>
                        val roeMap = document.get("roe") as? Map<*, *>
                        val derMap = document.get("der") as? Map<*, *>

                        if (name != null && code != null && gpmMap != null && npmMap != null && roeMap != null && derMap != null) {
                            val gpmStock = gpmMap["gpm_stock"] as? String
                            val npmStock = npmMap["npm_stock"] as? String
                            val roeStock = roeMap["roe_stock"] as? String
                            val derStock = derMap["der_stock"] as? String

                            val stock = DetailCompany(
                                stockId,
                                name,
                                code,
                                gpm(gpmStock),
                                npm(npmStock),
                                roe(roeStock),
                                der(derStock)
                            )

                            // Add the stock to the list
                            list.add(stock)

                            // Store the stockId in the map
                            stockIdMap[stockId] = stockId
                        }
                        Log.d("TAG_ambil", "${document.id} => ${document.data}")
                    }
                    list.sortBy { it.code }

                    binding.tvSpk.visibility = View.INVISIBLE
                    stockAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(applicationContext, "Data gagal di ambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                    binding.tvSpk.visibility = View.VISIBLE
                }
                progresDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mendapat data", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Error getting documents: ", exception)
                progresDialog.dismiss()
                binding.tvSpk.visibility = View.VISIBLE
            }
    }

    override fun onCardDetailClicked(id: String?) {
        val stockId = stockIdMap[id] // Retrieve stockId using the name
        if (stockId != null) {
            val action = Intent(this, DetailStockActivity::class.java)
            action.putExtra("id", stockId)
            startActivity(action)
            //finish()
            Log.d("TAG_id", "lempar id: $stockId")
        } else {
            // Handle the case where the name doesn't correspond to a stockId
            Toast.makeText(this, "StockId not found for $id", Toast.LENGTH_SHORT).show()
        }
    }

}