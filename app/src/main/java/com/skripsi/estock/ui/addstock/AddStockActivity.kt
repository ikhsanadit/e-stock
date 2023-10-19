package com.skripsi.estock.ui.addstock

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityAddStockBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.stockchart.StockChartActivity

class AddStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStockBinding
    private lateinit var progressDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Menambah data saham")
        progressDialog.setMessage("Silahkan Tunggu")

        binding.apply {
            btnAdd.setSafeOnClickListener {
                val companyName = etFillCompanyName.text.toString()
                val companyCode = etFillCompanyCode.text.toString()
                val companyGpm = etFillGpm.text.toString().toDouble()
                val companyNpm = etFillNpm.text.toString().toDouble()
                val companyRoe = etFillRoe.text.toString().toDouble()
                val companyDer = etFillDer.text.toString().toDouble()

                if (validateInput(companyName, companyCode, companyGpm, companyNpm, companyRoe, companyDer)) {
                    addData(companyName, companyCode, companyGpm, companyNpm, companyRoe, companyDer)
                } else {
                    Toast.makeText(applicationContext, "Silahkan Isi Semua Data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInput(
        companyName: String,
        companyCode: String,
        companyGpm: Double,
        companyNpm: Double,
        companyRoe: Double,
        companyDer: Double
    ): Boolean {
        return companyName.isNotEmpty() && companyCode.isNotEmpty() &&
                companyGpm.toString().isNotEmpty() && companyNpm.toString().isNotEmpty() &&
                companyRoe.toString().isNotEmpty() && companyDer.toString().isNotEmpty()
    }

    private fun calculateSpk(value: Double): Int {
        return when {
            value <= 1.0 -> 1
            value <= 10.0 -> 2
            else -> 3
        }
    }

    private fun calculateGpmSpk(value: Double): Int {
        return when {
            value <= 10.0 -> 1
            value <= 40.0 -> 2
            else -> 3
        }
    }

    private fun calculateDerSpk(value: Double): Int {
        return when {
            value <= 10.0 -> 3
            value < 100.0 -> 2
            else -> 1
        }
    }

    private fun addData(
        cName: String,
        cCode: String,
        cGpm: Double,
        cNpm: Double,
        cRoe: Double,
        cDer: Double
    ) {
        val gpmValue = cGpm
        val npmValue = cNpm
        val roeValue = cRoe
        val derValue = cDer

        val gpmSpk = calculateGpmSpk(gpmValue)
        val npmSpk = calculateSpk(npmValue)
        val roeSpk = calculateSpk(roeValue)
        val derSpk = calculateDerSpk(derValue)

        val detailCompany = hashMapOf(
            "name" to cName,
            "code" to cCode,
            "gpm" to hashMapOf(
                "gpm_stock" to cGpm,
                "gpm_spk" to gpmSpk
            ),
            "npm" to hashMapOf(
                "npm_stock" to cNpm,
                "npm_spk" to npmSpk
            ),
            "roe" to hashMapOf(
                "roe_stock" to cRoe,
                "roe_spk" to roeSpk
            ),
            "der" to hashMapOf(
                "der_stock" to cDer,
                "der_spk" to derSpk
            )
        )

        progressDialog.show()
        firestoreDb.collection("detail company")
            .add(detailCompany)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Berhasil menambah data saham", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Detail added with ID: ${documentReference.id}")
                progressDialog.dismiss()
                startActivity(Intent(this, StockChartActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                Log.w("TAG", "Error adding document", e)
                progressDialog.dismiss()
            }
    }
}