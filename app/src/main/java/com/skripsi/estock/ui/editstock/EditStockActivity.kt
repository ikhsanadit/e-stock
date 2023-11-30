package com.skripsi.estock.ui.editstock

import android.app.AlertDialog
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
    private lateinit var progressDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    private var idStock: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        idStock = intent.getStringExtra("id")
        if (idStock != null) {
            Log.d("TAG_Detail", "dapat id = $idStock")
        } else {
            Log.w("TAG_Detail", "No 'id' extra found in the intent")
        }

        setupDataFields()

        binding.btnSave.setSafeOnClickListener {
            showEditConfirmation()
        }
    }

    fun showEditConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Edit")
        builder.setMessage("Apakah kamu yakin akan merubah data saham ini?")

        // Set up the buttons
        builder.setPositiveButton("Edit") { dialog, which ->
            updateData()
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            // User clicked Cancel button
            Toast.makeText(this, "Batal Menghapus", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun updateData() {
        val companyName = binding.etFillCompanyName.text.toString()
        val companyCode = binding.etFillCompanyCode.text.toString()
        val gpm = binding.etFillGpm.text.toString()
        val npm = binding.etFillNpm.text.toString()
        val roe = binding.etFillRoe.text.toString()
        val der = binding.etFillDer.text.toString()

        val gpmSpk = calculateGpmSpk(gpm)
        val npmSpk = calculateSpk(npm)
        val roeSpk = calculateSpk(roe)
        val derSpk = calculateDerSpk(der)

        val detailCompany = hashMapOf(
            "name" to companyName,
            "code" to companyCode,
            "gpm" to hashMapOf(
                "gpm_stock" to gpm,
                "gpm_spk" to gpmSpk
            ),
            "npm" to hashMapOf(
                "npm_stock" to npm,
                "npm_spk" to npmSpk
            ),
            "roe" to hashMapOf(
                "roe_stock" to roe,
                "roe_spk" to roeSpk
            ),
            "der" to hashMapOf(
                "der_stock" to der,
                "der_spk" to derSpk
            )
        )

        progressDialog.setMessage("Mengubah data")
        progressDialog.show()
        firestoreDb.collection("detail company")
            .document(idStock ?: "id")
            .update(detailCompany as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Berhasil merubah data saham", Toast.LENGTH_SHORT).show()
                val updatedIntent = Intent(this, DetailStockActivity::class.java)
                updatedIntent.putExtra("id", idStock)
                startActivity(updatedIntent)
                finish()
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengubah data", Toast.LENGTH_SHORT).show()
                Log.w("TAG", "Error updating document", e)
                progressDialog.dismiss()
            }
    }

    private fun setupDataFields() {
        val name = intent.getStringExtra("name")
        val code = intent.getStringExtra("code")
        val gpm = intent.getStringExtra("gpm")
        val npm = intent.getStringExtra("npm")
        val roe = intent.getStringExtra("roe")
        val der = intent.getStringExtra("der")

        binding.etFillCompanyName.setText(name)
        binding.etFillCompanyCode.setText(code)
        binding.etFillGpm.setText(gpm)
        binding.etFillNpm.setText(npm)
        binding.etFillRoe.setText(roe)
        binding.etFillDer.setText(der)
    }

    private fun calculateSpk(value: String): Int {
        val floatValue = value.toFloat()
        return when {
            floatValue <= 1.0 -> 1
            floatValue <= 10.0 -> 2
            else -> 3
        }
    }

    private fun calculateGpmSpk(value: String): Int {
        val floatValue = value.toFloat()
        return when {
            floatValue <= 10.0 -> 1
            floatValue <= 40.0 -> 2
            else -> 3
        }
    }

    private fun calculateDerSpk(value: String): Int {
        val floatValue = value.toFloat()
        return when {
            floatValue <= 10.0 -> 3
            floatValue < 100.0 -> 2
            else -> 1
        }
    }
}