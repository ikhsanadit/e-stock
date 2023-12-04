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
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.skripsi.estock.ui.stockchart.StockChartActivity
import kotlin.math.pow
import kotlin.math.sqrt

class SpkActivity : AppCompatActivity(){
    private lateinit var binding: ActivitySpkBinding
    private lateinit var progresDialog: ProgressDialog
    private lateinit var stockAdapter: SpkListAdapter

    private var list: MutableList<DetailCompany> = mutableListOf()
    private val alternatifList: MutableList<DetailCompany> = mutableListOf()
    private var idStock: String? = ""
    private var listWeight = mutableListOf<String>()
    private var weightC1 = ""
    private var weightC2 = ""
    private var weightC3 = ""
    private var weightC4 = ""

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCollection = Firebase.firestore.collection("users")

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

        if (userId != null) {
            usersCollection.document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the role from the document
                        val role = documentSnapshot.getLong("role")
                        if (role != null) {
                            Log.d("TAG_Role", "role: $role")
                            // Use '==' for comparison, '=' is for assignment
                            if (role.toInt() == 1) {
                                binding.tbTableCriteria.visibility = View.VISIBLE
                                binding.tvEdit.visibility = View.VISIBLE
                                binding.tvWeight.visibility = View.VISIBLE
                            } else if (role.toInt() == 2) {
                                binding.tbTableCriteria.visibility = View.GONE
                                binding.tvEdit.visibility = View.GONE
                                binding.tvWeight.visibility = View.GONE
                            } else {
                                Log.e("TAG", "Invalid role value: $role")
                            }
                        } else {
                            Log.e("TAG", "Role field is null")
                        }
                    } else {
                        Log.e("TAG", "User document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TAG", "Error getting user document: $e")
                }
        } else {
            Log.e("TAG", "User ID is null")
        }

        getData()


        binding.tvEdit.setSafeOnClickListener {
            showCustomDialog()
        }

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

                                        for (alternatif in alternatifList) {
                                            val minC4 = alternatifList.minByOrNull { it.der.der_spk!!.toDouble() }?.der?.der_spk!!.toDouble()
                                            alternatif.derSpkNormalisasi = minC4!! / alternatif.der.der_spk!!.toDouble()

                                            val maxC1 = alternatifList.maxByOrNull { it.gpm.gpm_spk!!.toDouble() }?.gpm?.gpm_spk!!.toDouble()
                                            val maxC2 = alternatifList.maxByOrNull { it.npm.npm_spk!!.toDouble() }?.npm?.npm_spk!!.toDouble()
                                            val maxC3 = alternatifList.maxByOrNull { it.roe.roe_spk!!.toDouble() }?.roe?.roe_spk!!.toDouble()

                                            alternatif.gpmSpkNormalisasi = alternatif.gpm.gpm_spk!!.toDouble() / maxC1!!
                                            alternatif.npmSpkNormalisasi = alternatif.npm.npm_spk!!.toDouble() / maxC2!!
                                            alternatif.roeSpkNormalisasi = alternatif.roe.roe_spk!!.toDouble() / maxC3!!

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

                                            val rSolutionY = sqrt(gpmY + npmY + roeY + derY)
                                            Log.d("TAG_jaraknegatif", "jarak solusi ideal negatif: $rSolutionY")

                                            alternatif.nilaiPreferensi = rSolutionY / (rSolutionX + rSolutionY)
                                            Log.d("TAG sc", "sc: ${alternatif.nilaiPreferensi}")
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

                        binding.apply {
                            tvWGpm.text = wGpm.toString()
                            tvWNpm.text = wNpm.toString()
                            tvWRoe.text = wRoe.toString()
                            tvWDer.text = wDer.toString()
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
                    list.sortWith(compareByDescending<DetailCompany> { it.gpm.gpm_spk }
                        .thenByDescending { it.npm.npm_spk }
                        .thenByDescending { it.roe.roe_spk }
                        .thenBy { it.der.der_spk })

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

    private fun showCustomDialog() {
        var cweightC1 = 0.0
        var cweightC2 = 0.0
        var cweightC3 = 0.0
        var cweightC4 = 0.0

        val dialogBinding: DialogCriteriaBinding? =
            DialogCriteriaBinding.inflate(LayoutInflater.from(this), null, false)

        val customDialog = AlertDialog.Builder(this, 0).create()

        customDialog.apply {
            setView(dialogBinding?.root)
            setCancelable(false)
        }.show()

        listWeight = mutableListOf("Pilih Bobot","10 %", "20 %", "30 %", "40 %", "50 %", "60 %", "70 %", "90 %", "100 %")
        val bankDropdown = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, listWeight)
        dialogBinding?.apply {
            dropdownGpm.setAdapter(bankDropdown)
            dropdownGpm.setText(listWeight[0], false)
            dialogBinding.dropdownGpm.setOnItemClickListener{
                    _, _, position, _ ->
                weightC1 = listWeight[position]
                Log.d("weight", "C1: $weightC1" )
                if (weightC1 == "10 %"){
                    cweightC1 = 0.1
                }else if (weightC1 == "20 %"){
                    cweightC1 = 0.2
                } else if (weightC1 == "30 %"){
                    cweightC1 = 0.3
                } else if (weightC1 == "40 %"){
                    cweightC1 = 0.4
                } else if (weightC1 == "50 %"){
                    cweightC1 = 0.5
                } else if (weightC1 == "60 %"){
                    cweightC1 = 0.6
                } else if (weightC1 == "70 %"){
                    cweightC1 = 0.7
                }else if (weightC1 == "80 %"){
                    cweightC1 = 0.8
                }else if (weightC1 == "90 %"){
                    cweightC1 = 0.9
                }else if (weightC1 == "100 %"){
                    cweightC1 = 1.0
                }
            }

            dropdownNpm.setAdapter(bankDropdown)
            dropdownNpm.setText(listWeight[0], false)
            dialogBinding.dropdownNpm.setOnItemClickListener{
                    _, _, position, _ ->
                weightC2 = listWeight[position]
                Log.d("weight", "C2: $weightC2 " )
                if (weightC2 == "10 %"){
                    cweightC2 = 0.1
                }else if (weightC2 == "20 %"){
                    cweightC2 = 0.2
                } else if (weightC2 == "30 %"){
                    cweightC2 = 0.3
                } else if (weightC2 == "40 %"){
                    cweightC2 = 0.4
                } else if (weightC2 == "50 %"){
                    cweightC2 = 0.5
                } else if (weightC2 == "60 %"){
                    cweightC2 = 0.6
                } else if (weightC2 == "70 %"){
                    cweightC2 = 0.7
                }else if (weightC2 == "80 %"){
                    cweightC2 = 0.8
                }else if (weightC2 == "90 %"){
                    cweightC2 = 0.9
                }else if (weightC1 == "100 %"){
                    cweightC2 = 1.0
                }
            }

            dropdownRoe.setAdapter(bankDropdown)
            dropdownRoe.setText(listWeight[0], false)
            dialogBinding.dropdownRoe.setOnItemClickListener{
                    _, _, position, _ ->
                weightC3 = listWeight[position]
                Log.d("weight", "C3: $weightC3 " )
                if (weightC3 == "10 %"){
                    cweightC3 = 0.1
                }else if (weightC3 == "20 %"){
                    cweightC3 = 0.2
                } else if (weightC3 == "30 %"){
                    cweightC3 = 0.3
                } else if (weightC3 == "40 %"){
                    cweightC3 = 0.4
                } else if (weightC3 == "50 %"){
                    cweightC3 = 0.5
                } else if (weightC3 == "60 %"){
                    cweightC3 = 0.6
                } else if (weightC3 == "70 %"){
                    cweightC3 = 0.7
                }else if (weightC3 == "80 %"){
                    cweightC3 = 0.8
                }else if (weightC3 == "90 %"){
                    cweightC3 = 0.9
                }else if (weightC3 == "100 %"){
                    cweightC3 = 1.0
                }
            }

            dropdownDer.setAdapter(bankDropdown)
            dropdownDer.setText(listWeight[0], false)
            dialogBinding.dropdownDer.setOnItemClickListener{
                    _, _, position, _ ->
                weightC4 = listWeight[position]
                Log.d("weight", "C4: $weightC4" )
                if (weightC4 == "10 %"){
                    cweightC4 = 0.1
                }else if (weightC4 == "20 %"){
                    cweightC4 = 0.2
                } else if (weightC4 == "30 %"){
                    cweightC4 = 0.3
                } else if (weightC4 == "40 %"){
                    cweightC4 = 0.4
                } else if (weightC4 == "50 %"){
                    cweightC4 = 0.5
                } else if (weightC4 == "60 %"){
                    cweightC4 = 0.6
                } else if (weightC4 == "70 %"){
                    cweightC4 = 0.7
                }else if (weightC4 == "80 %"){
                    cweightC4 = 0.8
                }else if (weightC4 == "90 %"){
                    cweightC4 = 0.9
                }else if (weightC4 == "100 %"){
                    cweightC4 = 1.0
                }
            }
        }

        dialogBinding?.btnSave?.setSafeOnClickListener {
            Log.d("weight", "C1: $cweightC1 C2: $cweightC2 C3: $cweightC3 C4: $cweightC4")
            var total = (cweightC1 + cweightC2 + cweightC3 + cweightC4)
            Log.d("TAG_total", "Total: $total ")
            if ( total == 1.0 || total == 0.9999999999999999) {
                val criteriaWeight = hashMapOf(
                    "gpm_weight" to cweightC1,
                    "npm_weight" to cweightC2,
                    "roe_weight" to cweightC3,
                    "der_weight" to cweightC4
                )
                progresDialog.show()
                firestoreDb.collection("criteria weight")
                    .document(wId)
                    .update(criteriaWeight as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Berhasil merubah bobot", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SpkActivity::class.java))
                        progresDialog.dismiss()
                        customDialog.dismiss()
                        recreate()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal merubah bobot", Toast.LENGTH_SHORT).show()
                        Log.w("TAG", "Error updating document", e)
                        progresDialog.dismiss()
                    }
            } else {
                Toast.makeText(this, "Total jumlah bobot harus 100%", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBinding?.btnCancel?.setSafeOnClickListener {
            customDialog.dismiss()
        }
    }


}
