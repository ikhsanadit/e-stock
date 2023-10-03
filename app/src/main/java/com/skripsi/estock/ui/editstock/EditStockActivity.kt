package com.skripsi.estock.ui.editstock

import android.R.id
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityEditStockBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.detailstock.DetailStockActivity

class EditStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStockBinding
    private lateinit var progresDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    var idStock : String? = null
    var name : String? =null
    var code : String? = null
    var gpm : String? = null
    var npm : String? = null
    var roe : String? = null
    var der : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progresDialog = ProgressDialog(this)


        idStock = intent.getStringExtra("id")
        if (idStock != null) {
            Log.d("TAG_Detail", "dapat id = $idStock")
        } else {
            Log.w("TAG_Detail", "No 'id' extra found in the intent")
        }
        getData()

        binding.apply {
            btnSave.setSafeOnClickListener {
                var cname = binding.etFillCompanyName.text.toString()
                var ccode = binding.etFillCompanyCode.text.toString()
                var cgpm = binding.etFillGpm.text.toString()
                var cnpm = binding.etFillNpm.text.toString()
                var croe = binding.etFillRoe.text.toString()
                var cder = binding.etFillDer.text.toString()

                updateData(cname, ccode, cgpm, cnpm, croe, cder)

            }
        }
    }

    private fun updateData(cName: String, cCode: String, cGpm: String, cNpm: String, cRoe: String, cDer: String) {
        val detailCompany = mapOf(
            "name" to cName,
            "code" to cCode,
            "gpm" to cGpm,
            "npm" to cNpm,
            "roe" to cRoe,
            "der" to cDer
        )
        progresDialog.setMessage("Mengubah data")
        progresDialog.show()
        firestoreDb.collection("detail company")
            .document(idStock ?: "id")
            .update(detailCompany)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Berhasil merubah data saham", Toast.LENGTH_SHORT).show()
                val updatedIntent = Intent(this, DetailStockActivity::class.java)
                updatedIntent.putExtra("id", idStock)
                startActivity(updatedIntent)
                progresDialog.dismiss()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "gagal menambah", Toast.LENGTH_SHORT).show()
                Log.w("TAG", "Error adding document", e)
                progresDialog.dismiss()
            }
    }

    private fun getData() {
        binding.apply {
            etFillCompanyName.setText(intent.getStringExtra("name"))
            etFillCompanyCode.setText(intent.getStringExtra("code"))
            etFillGpm.setText(intent.getStringExtra("gpm"))
            etFillNpm.setText(intent.getStringExtra("npm"))
            etFillRoe.setText(intent.getStringExtra("roe"))
            etFillDer.setText(intent.getStringExtra("der"))

        }
    }
}