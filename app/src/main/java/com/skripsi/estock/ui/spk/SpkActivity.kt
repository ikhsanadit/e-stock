package com.skripsi.estock.ui.spk

import android.R
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivitySpkBinding
import com.skripsi.estock.databinding.DialogCriteriaBinding
import com.skripsi.estock.datasource.model.*
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.spk.adapter.SpkListAdapter
import kotlin.math.pow
import kotlin.math.sqrt

class SpkActivity : AppCompatActivity(){
    private lateinit var binding: ActivitySpkBinding
    private lateinit var progresDialog: ProgressDialog
    private lateinit var stockAdapter: SpkListAdapter

    private var list: MutableList<DetailCompany> = mutableListOf()
    private val alternatifList: MutableList<DetailCompany> = mutableListOf()
    private var idStock: String? = ""

    private val firestoreDb = Firebase.firestore

    private var bobotC1 = 0.0
    private var bobotC2 = 0.0
    private var bobotC3 = 0.0
    private var bobotC4 = 0.0

    private val wId = "mK1XufxOAEdhG8jxVYQq"

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

        getData()

        stockAdapter = SpkListAdapter(list)

        binding.rvListStock.layoutManager = LinearLayoutManager(this)
        binding.rvListStock.adapter = stockAdapter

    }

    private fun spkSAWandTOPSISMethod() {
        progresDialog.show()
        firestoreDb.collection("criteria weight")
            .document(wId)
            .get()
            .addOnCompleteListener { task ->
                list.clear()
                Log.d("TAG task", "isi task: $task ")
                if (task.isSuccessful) {
                    val document = task.result!!
                    val wGpm = document.getDouble("gpm_weight")
                    val wNpm = document.getDouble("npm_weight")
                    val wRoe = document.getDouble("roe_weight")
                    val wDer = document.getDouble("der_weight")

                    if ( wGpm != null && wNpm != null && wRoe != null && wDer != null) {
                        val criteria = WeightCriteria()
                        criteria.gpm_weight = wGpm
                        criteria.npm_weight = wNpm
                        criteria.roe_weight = wRoe
                        criteria.der_weight = wDer

                        bobotC1 = wGpm
                        bobotC2 = wNpm
                        bobotC3 = wRoe
                        bobotC4 = wDer

                        firestoreDb.collection("detail company")
                            .get()
                            .addOnCompleteListener { doc ->
                                Log.d("TAG Doc", "isi Doc: $doc ")
                                list.clear()
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
                                        Log.d("TAG_get_spk", "spkSAWandTOPSISMethod: ${document.id} => ${document.data}")
                                    }

                                    if (alternatifList.isNotEmpty()) {
                                        //normalisasi
                                        for (alternatif in alternatifList) {

                                            val maxC1 = alternatifList.maxByOrNull { it.gpm.gpm_spk!!.toDouble() }?.gpm?.gpm_spk!!.toDouble()
                                            val maxC2 = alternatifList.maxByOrNull { it.npm.npm_spk!!.toDouble() }?.npm?.npm_spk!!.toDouble()
                                            val maxC3 = alternatifList.maxByOrNull { it.roe.roe_spk!!.toDouble() }?.roe?.roe_spk!!.toDouble()
                                            val minC4 = alternatifList.minByOrNull { it.der.der_spk!!.toDouble() }?.der?.der_spk!!.toDouble()

                                            alternatif.gpmSpkNormalisasi = alternatif.gpm.gpm_spk!!.toDouble() / maxC1!!
                                            alternatif.npmSpkNormalisasi = alternatif.npm.npm_spk!!.toDouble() / maxC2!!
                                            alternatif.roeSpkNormalisasi = alternatif.roe.roe_spk!!.toDouble() / maxC3!!
                                            alternatif.derSpkNormalisasi = minC4!! / alternatif.der.der_spk!!.toDouble()

                                            Log.d("TAG_Gpm", "gpm Ternormalisasi: ${alternatif.gpmSpkNormalisasi}")
                                            Log.d("TAG_Npm", "npm Ternormalisasi: ${alternatif.npmSpkNormalisasi}")
                                            Log.d("TAG_Roe", "roe Ternormalisasi: ${alternatif.roeSpkNormalisasi}")
                                            Log.d("TAG_Der", "der Ternormalisasi: ${alternatif.derSpkNormalisasi}")

                                        }
                                        // pembobotan
                                        Log.d("TAG_bobot", "bobot spk: $bobotC1 $bobotC2 $bobotC3 $bobotC4")
                                        for (i in alternatifList.indices) {
                                            alternatifList[i].c1xW = alternatifList[i].gpmSpkNormalisasi * bobotC1
                                            alternatifList[i].c2xW = alternatifList[i].npmSpkNormalisasi * bobotC2
                                            alternatifList[i].c3xW = alternatifList[i].roeSpkNormalisasi * bobotC3
                                            alternatifList[i].c4xW = alternatifList[i].derSpkNormalisasi * bobotC4

                                            Log.d("TAG_Gpm", "gpm Terbobot: ${alternatifList[i].c1xW}")
                                            Log.d("TAG_Npm", "npm Terbobot: ${alternatifList[i].c2xW}")
                                            Log.d("TAG_Roe", "roe Terbobot: ${alternatifList[i].c3xW}")
                                            Log.d("TAG_Der", "der Terbobot: ${alternatifList[i].c4xW}")
                                        }

                                        //Solusi ideal positif
                                        val maxC1 = alternatifList.maxOf { it.c1xW }
                                        val maxC2 = alternatifList.maxOf { it.c2xW }
                                        val maxC3 = alternatifList.maxOf { it.c3xW }
                                        val minC4 = alternatifList.minOf { it.c4xW }

                                        Log.d("TAG_Solusi_Ideal Positif", "C1:$maxC1, C2:$maxC2, C3:$maxC3, C4:$minC4 ")

                                        //Solusi ideal negatif
                                        val minC1 = alternatifList.minOf { it.c1xW }
                                        val minC2 = alternatifList.minOf { it.c2xW }
                                        val minC3 = alternatifList.minOf { it.c3xW }
                                        val maxC4 = alternatifList.maxOf { it.c4xW }

                                        Log.d("TAG_Solusi_Ideal negatif", "C1:$minC1, C2:$minC2, C3:$minC3, C4:$maxC4 ")

                                        for (alternatif in alternatifList) {
                                            val derX = (alternatif.c4xW - minC4).pow(2)
                                            val gpmX = (alternatif.c1xW - maxC1).pow(2)
                                            val npmX = (alternatif.c2xW - maxC2).pow(2)
                                            val roeX = (alternatif.c3xW - maxC3).pow(2)

                                            val rSolutionX = sqrt(gpmX + npmX + roeX + derX)
                                            Log.d("TAG_jarakpositif", "jarak solusi ideal positif: $rSolutionX")

                                            val derY = (alternatif.c4xW - maxC4).pow(2)
                                            val gpmY = (alternatif.c1xW - minC1).pow(2)
                                            val npmY = (alternatif.c2xW - minC2).pow(2)
                                            val roeY = (alternatif.c3xW - minC3).pow(2)

                                            // Jarak solusi ideal positif dan negatif
                                            val rSolutionY = sqrt(gpmY + npmY + roeY + derY)
                                            Log.d("TAG_jaraknegatif", "jarak solusi ideal negatif: $rSolutionY")
                                            // Kedekatan Relatif / Nilai Preferensi
                                            alternatif.nilaiPreferensi = rSolutionY / (rSolutionX + rSolutionY)
                                            Log.d("TAG sc", "sc: ${alternatif.nilaiPreferensi}")
                                        }
//                                        var gpmN = 0.0
//                                        var npmN = 0.0
//                                        var roeN = 0.0
//                                        var derN = 0.0
//
//                                        var gpmNTb = 0.0
//                                        var npmNTb = 0.0
//                                        var roeNTb = 0.0
//                                        var derNTb = 0.0
//
//                                        var gpmPlus = 0.0
//                                        var npmPlus = 0.0
//                                        var roePlus = 0.0
//                                        var derPlus = 0.0
//
//                                        var gpmMinus = 0.0
//                                        var npmMinus = 0.0
//                                        var roeMinus = 0.0
//                                        var derMinus = 0.0

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
                                    getData()
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
                    Log.d("TAG_ambil", "${document.id} => ${document.data}")
                    stockAdapter.notifyDataSetChanged()
                    binding.tvSpk.visibility = View.INVISIBLE
                } else {
                    Toast.makeText(applicationContext, "Data gagal di ambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                    binding.tvSpk.visibility = View.VISIBLE
                }
                progresDialog.dismiss()
            }



    }

    private fun getData() {
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
//                    list.sortWith(compareByDescending<DetailCompany> { it.gpm.gpm_spk }
//                        .thenByDescending { it.npm.npm_spk }
//                        .thenByDescending { it.roe.roe_spk }
//                        .thenBy { it.der.der_spk })

                    stockAdapter.notifyDataSetChanged()
                    getResult()
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

    private fun getResult(){
        firestoreDb.collection("detail company")
            .orderBy("spk_score", Query.Direction.DESCENDING)
            .limit(1)
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


                        if (name != null && code != null && gpmSpk != null && npmSpk != null && roeSpk != null && derSpk != null) {
                            binding.tvName.text = name

                        }
                        Log.d("TAG_ambils", "${document.id} => ${document.data}")
                    }

                } else {
                    Toast.makeText(applicationContext, "Data gagal di ambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mendapat data", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Error getting documents: ", exception)

            }
    }




}
