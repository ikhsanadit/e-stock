package com.skripsi.estock.ui.spk

import android.R
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivitySpkBinding
import com.skripsi.estock.datasource.model.*
import com.skripsi.estock.ui.spk.adapter.SpkListAdapter
import kotlin.math.pow
import kotlin.math.sqrt


class SpkActivity : AppCompatActivity(), SpkListAdapter.SpkClickListener {
    private lateinit var binding: ActivitySpkBinding
    private lateinit var progresDialog: ProgressDialog
    private lateinit var stockAdapter: SpkListAdapter

    private var list: MutableList<DetailCompany> = mutableListOf()
    private val alternatifList: MutableList<DetailCompany> = mutableListOf()
    private var idStock: String? = ""

    private val firestoreDb = Firebase.firestore

    private val bobotC1 = 0.5
    private val bobotC2 = 0.2
    private val bobotC3 = 0.2
    private val bobotC4 = 0.1

    override fun onStart() {
        super.onStart()
        spkSAWandTOPSISMethod()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        progresDialog = ProgressDialog(this)
        progresDialog.setMessage("Silahkan Tunggu")

        spkSAWandTOPSISMethod()

        getData()


    }

    private fun spkSAWandTOPSISMethod() {
        firestoreDb.collection("detail company")
            .get()
            .addOnCompleteListener { doc ->
                Log.d("TAG Doc", "isi Doc: $doc ")
                if (doc.isSuccessful) {
                    for (document in doc.result!!) {
                        // Extract data from Firebase document
                        val id =  document.id
                        val name = document.getString("name")
                        val code = document.getString("code")
                        val gpmSpk = document.getDouble("gpm.gpm_spk")
                        val npmSpk = document.getDouble("npm.npm_spk")
                        val roeSpk = document.getDouble("roe.roe_spk")
                        val derSpk = document.getDouble("der.der_spk")

                        if (name != null && code != null && gpmSpk != null && npmSpk != null && roeSpk != null && derSpk != null) {
                            val stock = DetailCompany()
                            stock.id = id
                            stock.name = name
                            stock.code = code
                            stock.gpm.gpm_spk = gpmSpk
                            stock.npm.npm_spk = npmSpk
                            stock.roe.roe_spk = roeSpk
                            stock.der.der_spk = derSpk
                            alternatifList.add(stock)
                        }
                    }

                    if (alternatifList.isNotEmpty()) {

                        for (alternatif in alternatifList) {
                            val minC4 = alternatifList.minByOrNull { it.der.der_spk!!.toDouble() }?.der?.der_spk!!.toDouble()
                            alternatif.derSpkNormalisasi = minC4!! / alternatif.der.der_spk!!.toDouble()

                            val maxC1 = alternatifList.maxByOrNull { it.gpm.gpm_spk!!.toDouble() }?.gpm?.gpm_spk!!.toDouble()
                            val maxC2 = alternatifList.maxByOrNull { it.npm.npm_spk!!.toDouble() }?.npm?.npm_spk!!.toDouble()
                            val maxC3 = alternatifList.maxByOrNull { it.roe.roe_spk!!.toDouble() }?.roe?.roe_spk!!.toDouble()

                            alternatif.gpmSpkNormalisasi = alternatif.gpm.gpm_spk!!.toDouble() / maxC1!!
                            alternatif.npmSpkNormalisasi = alternatif.npm.npm_spk!!.toDouble() / maxC2!!
                            alternatif.roeSpkNormalisasi = alternatif.roe.roe_spk!!.toDouble() / maxC3!!

                            Log.d("TAG_gpm", "normalisasi gpm: ${alternatif.gpmSpkNormalisasi}")
                            Log.d("TAG_npm", "normalisasi npm: ${alternatif.npmSpkNormalisasi}")
                            Log.d("TAG_roe", "normalisasi roe: ${alternatif.roeSpkNormalisasi}")
                            Log.d("TAG_der", "normalisasi der: ${alternatif.derSpkNormalisasi}")
                        }
                        // pembobotan
                        for (i in alternatifList.indices) {
                            alternatifList[i].c1xW = alternatifList[i].gpmSpkNormalisasi * bobotC1
                            alternatifList[i].c2xW = alternatifList[i].npmSpkNormalisasi * bobotC2
                            alternatifList[i].c3xW = alternatifList[i].roeSpkNormalisasi * bobotC3
                            alternatifList[i].c4xW = alternatifList[i].derSpkNormalisasi * bobotC4
                        }
                        //Solusi ideal positif
                        val maxC1 = alternatifList.maxOf { it.c1xW }
                        val maxC2 = alternatifList.maxOf { it.c2xW }
                        val maxC3 = alternatifList.maxOf { it.c3xW }
                        val minC4 = alternatifList.minOf { it.c4xW }
                        //Solusi ideal negatif
                        val minC1 = alternatifList.minOf { it.c1xW }
                        val minC2 = alternatifList.minOf { it.c2xW }
                        val minC3 = alternatifList.minOf { it.c3xW }
                        val maxC4 = alternatifList.maxOf { it.c4xW }


                        for (alternatif in alternatifList) {
                            val derX = (alternatif.c4xW - minC4).pow(2)
                            val gpmX = (alternatif.c1xW - maxC1).pow(2)
                            val npmX = (alternatif.c2xW - maxC2).pow(2)
                            val roeX = (alternatif.c3xW - maxC3).pow(2)

                            val rSolutionX = sqrt(gpmX + npmX + roeX + derX)

                            val derY = (alternatif.c4xW - maxC4).pow(2)
                            val gpmY = (alternatif.c1xW - minC1).pow(2)
                            val npmY = (alternatif.c2xW - minC2).pow(2)
                            val roeY = (alternatif.c3xW - minC3).pow(2)

                            val rSolutionY = sqrt(gpmY + npmY + roeY + derY)

                            alternatif.nilaiPreferensi = rSolutionY / (rSolutionX + rSolutionY)

                        }

                        var score = 0.0
                        for (i in alternatifList.indices){
                            idStock = alternatifList[i].id
                            score = alternatifList[i].nilaiPreferensi
                            Log.d("TAG_sc", "score: $score ")
                            firestoreDb.collection("detail company").document(idStock!!)
                                .update("spk_score", score)
                            Log.d("TAG_ID", "idStock: ${idStock}")
                        }

                    } else {
                        // Handle the case where the list is empty
                        Log.d("TAG", "alternatifList is empty")
                    }
                } else {
                    Toast.makeText(applicationContext, "Data gagal diambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                }
                progresDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mendapatkan data", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Error getting documents: ", exception)
                progresDialog.dismiss()
            }
    }

    private fun getData() {
        stockAdapter = SpkListAdapter(list)
        stockAdapter.listener = this

        binding.rvListStock.layoutManager = LinearLayoutManager(this)
        binding.rvListStock.adapter = stockAdapter

        progresDialog.show()
        firestoreDb.collection("detail company")
            .orderBy("spk_score", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                Log.d("TAG task", "isi task: $task ")
                list.clear()
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.id
                        val name = document.getString("name")
                        val code = document.getString("code")
                        val gpmSpk = document.getDouble("gpm.gpm_spk")
                        val npmSpk = document.getDouble("npm.npm_spk")
                        val roeSpk = document.getDouble("roe.roe_spk")
                        val derSpk = document.getDouble("der.der_spk")
                        val scoreSpk = document.getDouble("spk_score")

                        if (name != null && code != null && gpmSpk != null && npmSpk != null && roeSpk != null && derSpk != null) {
                            val stock = DetailCompany()
                            stock.id = id
                            stock.name = name
                            stock.code = code
                            stock.gpm.gpm_spk = gpmSpk
                            stock.npm.npm_spk = npmSpk
                            stock.roe.roe_spk = roeSpk
                            stock.der.der_spk = derSpk
                            stock.spk_score = scoreSpk
                            list.add(stock)
                        }
                        Log.d("TAG_ambil", "${document.id} => ${document.data}")
                    }
                    stockAdapter.notifyDataSetChanged()
                    binding.tvSpk.visibility = View.INVISIBLE
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
                binding.tvSpk.visibility = View.VISIBLE
                progresDialog.dismiss()
            }
    }

    override fun onCardDetailClicked(id: String?) {
        TODO("Not yet implemented")
    }

}
