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
                val companyGpm = etFillGpm.text.toString()
                val companyNpm = etFillNpm.text.toString()
                val companyRoe = etFillRoe.text.toString()
                val companyDer = etFillDer.text.toString()

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
        companyGpm: String,
        companyNpm: String,
        companyRoe: String,
        companyDer: String
    ): Boolean {
        return companyName.isNotEmpty() && companyCode.isNotEmpty() &&
                companyGpm.isNotEmpty() && companyNpm.isNotEmpty() &&
                companyRoe.isNotEmpty() && companyDer.isNotEmpty()
    }

    private fun calculateSpk(value: Float): Int {
        return when {
            value <= 1.0 -> 1
            value <= 10.0 -> 2
            else -> 3
        }
    }

    private fun calculateGpmSpk(value: Float): Int {
        return when {
            value <= 10.0 -> 1
            value <= 40.0 -> 2
            else -> 3
        }
    }

    private fun calculateDerSpk(value: Float): Int {
        return when {
            value <= 10.0 -> 3
            value < 100.0 -> 2
            else -> 1
        }
    }

    private fun addData(
        cName: String,
        cCode: String,
        cGpm: String,
        cNpm: String,
        cRoe: String,
        cDer: String
    ) {
        val gpmValue = cGpm.toFloat()
        val npmValue = cNpm.toFloat()
        val roeValue = cRoe.toFloat()
        val derValue = cDer.toFloat()

        val gpmSpk = calculateGpmSpk(gpmValue)
        val npmSpk = calculateSpk(npmValue)
        val roeSpk = calculateSpk(roeValue)
        val derSpk = calculateDerSpk(derValue)

        val detailCompany = hashMapOf(
            "name" to cName,
            "code" to cCode,
            "gpm" to hashMapOf(
                "gpm_stock" to cGpm,
                "gpm_spk" to gpmSpk.toString()
            ),
            "npm" to hashMapOf(
                "npm_stock" to cNpm,
                "npm_spk" to npmSpk.toString()
            ),
            "roe" to hashMapOf(
                "roe_stock" to cRoe,
                "roe_spk" to roeSpk.toString()
            ),
            "der" to hashMapOf(
                "der_stock" to cDer,
                "der_spk" to derSpk.toString()
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