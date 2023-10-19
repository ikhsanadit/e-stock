package com.skripsi.estock.ui.spk

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivitySpkBinding
import com.skripsi.estock.datasource.model.*
import kotlin.math.pow
import kotlin.math.sqrt

class SpkActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpkBinding
    private lateinit var progresDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    private val bobotC1 = 0.5
    private val bobotC2 = 0.2
    private val bobotC3 = 0.2
    private val bobotC4 = 0.1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        progresDialog = ProgressDialog(this)


    }

    override fun onStart() {
        super.onStart()
        spkSAWandTOPSISMethod()
    }

    private fun spkSAWandTOPSISMethod() {
        firestoreDb.collection("detail company")
            .get()
            .addOnCompleteListener { doc ->
                if (doc.isSuccessful) {
                    val alternatifList: MutableList<DetailCompany> = mutableListOf()
                    val alternatifList2: MutableList<DetailCompany> = mutableListOf()
                    for (document in doc.result!!) {
                        // Extract data from Firebase document
                        val name = document.getString("name")
                        val code = document.getString("code")
                        val gpmSpk = document.getString("gpm.gpm_spk")
                        val npmSpk = document.getString("npm.npm_spk")
                        val roeSpk = document.getString("roe.roe_spk")
                        val derSpk = document.getString("der.der_spk")

                        if (name != null && code != null && gpmSpk != null && npmSpk != null && roeSpk != null && derSpk != null) {
                            val stock = DetailCompany()
                            stock.name = name
                            stock.code = code
                            stock.gpm.gpm_spk = gpmSpk
                            stock.npm.npm_spk = npmSpk
                            stock.roe.roe_spk = roeSpk
                            stock.der.der_spk = derSpk
                            alternatifList.add(stock)
                        }
                    }

                    val normalizedMatrix = calculateNormalizedMatrix(alternatifList)
                    val weightedMatrix = calculateWeightedMatrix(normalizedMatrix)
                    val positiveIdealSolution = calculatePositiveIdealSolution(weightedMatrix)
                    val negativeIdealSolution = calculateNegativeIdealSolution(weightedMatrix)
                    val preferenceValues = calculatePreferenceValues(weightedMatrix, positiveIdealSolution, negativeIdealSolution)// send to db

                    // Find the best alternative based on preference values
                    val alternatifTerbaik = alternatifList.maxByOrNull{ it.nilaiPreferensi }

                    binding.tvSpk.text = alternatifTerbaik.toString()

                    // Print the results
                    println("Hasil SPK (C4 sebagai cost, C1, C2, dan C3 sebagai benefit):")
                    for (alternatif in alternatifList) {
                        Log.d("TAG","${alternatif.name}: Nilai Preferensi = ${alternatif.nilaiPreferensi}")
                    }

                    Log.d("TAG","Alternatif Terbaik: ${alternatifTerbaik?.name}")
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

    private fun calculateNormalizedMatrix(alternatifList: List<DetailCompany>): List<DetailCompany> {
        for (alternatif in alternatifList) {
            val minC4 = alternatifList.minByOrNull { it.der.der_spk!!.toDouble() }?.der?.der_spk!!.toDouble()
            alternatif.derSpkNormalisasi = minC4!! / alternatif.der.der_spk!!.toDouble()

            val maxC1 = alternatifList.maxByOrNull { it.gpm.gpm_spk!!.toDouble() }?.gpm?.gpm_spk!!.toDouble()
            val maxC2 = alternatifList.maxByOrNull { it.npm.npm_spk!!.toDouble() }?.npm?.npm_spk!!.toDouble()
            val maxC3 = alternatifList.maxByOrNull { it.roe.roe_spk!!.toDouble() }?.roe?.roe_spk!!.toDouble()

            alternatif.gpmSpkNormalisasi = alternatif.gpm.gpm_spk!!.toDouble() / maxC1!!
            alternatif.npmSpkNormalisasi = alternatif.npm.npm_spk!!.toDouble() / maxC2!!
            alternatif.roeSpkNormalisasi = alternatif.roe.roe_spk!!.toDouble() / maxC3!!
        }
        return alternatifList
    }

    private fun calculateWeightedMatrix(alternatifList: List<DetailCompany>): List<DetailCompany> {
        for (i in alternatifList.indices) {
            alternatifList[i].c1xW = alternatifList[i].gpmSpkNormalisasi * bobotC1
            alternatifList[i].c2xW = alternatifList[i].npmSpkNormalisasi * bobotC2
            alternatifList[i].c3xW = alternatifList[i].roeSpkNormalisasi * bobotC3
            alternatifList[i].c4xW = alternatifList[i].derSpkNormalisasi * bobotC4
        }
        return alternatifList
    }

    private fun calculatePositiveIdealSolution(weightedMatrix: List<DetailCompany>): List<Double> {
        val maxC1 = weightedMatrix.maxOf { it.c1xW }
        val maxC2 = weightedMatrix.maxOf { it.c2xW }
        val maxC3 = weightedMatrix.maxOf { it.c3xW }
        val minC4 = weightedMatrix.minOf { it.c4xW }
        return listOf(maxC1, maxC2, maxC3, minC4)
    }

    private fun calculateNegativeIdealSolution(weightedMatrix: List<DetailCompany>): List<Double> {
        val minC1 = weightedMatrix.minOf { it.c1xW }
        val minC2 = weightedMatrix.minOf { it.c2xW }
        val minC3 = weightedMatrix.minOf { it.c3xW }
        val maxC4 = weightedMatrix.maxOf { it.c4xW }
        return listOf(minC1, minC2, minC3, maxC4)
    }

    private fun calculatePreferenceValues(
        alternatifList: List<DetailCompany>,
        positiveIdealSolution: List<Double>,
        negativeIdealSolution: List<Double>
    ): List<Double> {
        val preferenceValues = mutableListOf<Double>()
        for (alternatif in alternatifList) {
            val derX = (alternatif.c4xW - negativeIdealSolution[3]).pow(2)
            val gpmX = (alternatif.c1xW - positiveIdealSolution[0]).pow(2)
            val npmX = (alternatif.c2xW - positiveIdealSolution[1]).pow(2)
            val roeX = (alternatif.c3xW - positiveIdealSolution[2]).pow(2)

            val rSolutionX = sqrt(gpmX + npmX + roeX + derX)

            val derY = (alternatif.c4xW - positiveIdealSolution[3]).pow(2)
            val gpmY = (alternatif.c1xW - negativeIdealSolution[0]).pow(2)
            val npmY = (alternatif.c2xW - negativeIdealSolution[1]).pow(2)
            val roeY = (alternatif.c3xW - negativeIdealSolution[2]).pow(2)

            val rSolutionY = sqrt(gpmY + npmY + roeY + derY)

            alternatif.nilaiPreferensi = rSolutionY / (rSolutionX + rSolutionY)
            preferenceValues.add(alternatif.nilaiPreferensi)
        }
        return preferenceValues
    }
}
