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
    private lateinit var progresDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Menambah data saham")
        progresDialog.setMessage("Silahkan Tunggu")

        val companyName = binding.etFillCompanyName
        val companyCode = binding.etFillCompanyCode
        val companyGpm = binding.etFillGpm
        val companyNpm = binding.etFillNpm
        val companyRoe = binding.etFillRoe
        val companyDer = binding.etFillDer

        binding.apply {
            btnAdd.setSafeOnClickListener {
                if (companyName.text.toString().isNotEmpty() && companyCode.text.toString()
                        .isNotEmpty() && companyGpm.text.toString().isNotEmpty() && companyNpm.text.toString()
                        .isNotEmpty() && companyRoe.text.toString().isNotEmpty() && companyDer.text.toString()
                        .isNotEmpty()
                ){
                    addData(companyName.text.toString(), companyCode.text.toString(), companyGpm.text.toString(), companyNpm.text.toString(), companyRoe.text.toString(), companyDer.text.toString())
                }else{
                    companyName.error
                    companyCode.error
                    companyGpm.error
                    companyNpm.error
                    companyRoe.error
                    companyDer.error

                    Toast.makeText(applicationContext, "Silahkan Isi Semua Data", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun addData(cName: String, cCode: String, cGpm: String, cNpm: String, cRoe: String, cDer: String) {
        val detailCompany = hashMapOf(
            "name" to cName,
            "code" to cCode,
            "gpm" to cGpm,
            "npm" to cNpm,
            "roe" to cRoe,
            "der" to cDer
        )

        progresDialog.show()
        firestoreDb.collection("detail company")
            .add(detailCompany)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Berhasil menambah data saham", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Detail added with ID: ${documentReference.id}")
                progresDialog.dismiss()
                startActivity(Intent(this, StockChartActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "gagal menambah", Toast.LENGTH_SHORT).show()
                Log.w("TAG", "Error adding document", e)
                progresDialog.dismiss()
            }

        changeSpkData(cName,cGpm,cNpm,cRoe,cDer)
    }

    private fun changeSpkData(cName: String, cGpm: String, cNpm: String, cRoe: String, cDer: String) {

       var gpm = "0"
        var npm = "0"
        var roe = "0"
        var der = "0"

        if (cGpm.toFloat() <= 10.0) {
            gpm = 1.toString()
        } else if (cGpm.toFloat() in 10.1..40.0) {
            gpm = 2.toString()
        } else if (cGpm.toFloat() >= 40.1) {
            gpm = 3.toString()
        }

        if (cNpm.toFloat() <= 1.0) {
            npm = 1.toString()
        } else if (cNpm.toFloat() in 1.1..10.0) {
            npm = 2.toString()
        } else if (cNpm.toFloat() >= 10.1) {
            npm = 3.toString()
        }

        if (cRoe.toFloat() <= 1.0) {
            roe = 1.toString()
        } else if (cRoe.toFloat() in 1.1..10.0) {
            roe = 2.toString()
        } else if (cRoe.toFloat() >= 10.1) {
            roe = 3.toString()
        }

        if (cDer.toFloat() <= 10.0) {
            der = 3.toString()
        } else if (cDer.toFloat() in 10.1..99.9) {
            der = 2.toString()
        } else if (cDer.toFloat() >= 100.0) {
            der = 1.toString()
        }

        Log.d("TAG_spk", "changeSpkData: $gpm, $npm, $roe, $der")

        val dataForSpk = hashMapOf(
            "name" to cName,
            "gpm" to gpm,
            "npm" to npm,
            "roe" to roe,
            "der" to der
        )

        firestoreDb.collection("spk")
            .add(dataForSpk)
            .addOnSuccessListener { documentReference2 ->
                //Toast.makeText(this, "Berhasil menambah data saham", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Data spk added with ID: ${documentReference2.id}")
                progresDialog.dismiss()
                finish()
            }
            .addOnFailureListener { e2 ->
                //Toast.makeText(this, e2.localizedMessage, Toast.LENGTH_SHORT).show()
                Log.w("TAG", "Error adding document", e2)
                progresDialog.dismiss()
            }

    }


}