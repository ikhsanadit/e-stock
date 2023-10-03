package com.skripsi.estock.ui.detailstock

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityDetailStockBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.editstock.EditStockActivity
import com.skripsi.estock.ui.stockchart.StockChartActivity

class DetailStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStockBinding
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
        binding = ActivityDetailStockBinding.inflate(layoutInflater)
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
            btnUpdate.setSafeOnClickListener {
                var update = Intent (this@DetailStockActivity, EditStockActivity::class.java)
                update.putExtra("id", idStock)
                update.putExtra("name", name)
                update.putExtra("code", code)
                update.putExtra("gpm", gpm)
                update.putExtra("npm", npm)
                update.putExtra("roe", roe)
                update.putExtra("der", der)
                startActivity(update)
                finish()
            }
            btnDelete.setSafeOnClickListener {
                deleteStock()
            }
        }

    }

    private fun deleteStock() {
        progresDialog.setMessage("Menghapus data")
        progresDialog.show()
        firestoreDb.collection("detail company")
            .document(idStock ?: "id")
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil meghapus data saham", Toast.LENGTH_SHORT).show()
                Log.d("TAG_Hapus", "DocumentSnapshot successfully deleted!")
                finish()
                startActivity(Intent(this, StockChartActivity::class.java))
                progresDialog.dismiss()
            }
            .addOnFailureListener { e->
                Log.w("Tag_Hapus", "Gagal menghapus")
                progresDialog.dismiss()
            }
    }

    private fun getData() {
        progresDialog.setMessage("Mengambil data")
        progresDialog.show()
        firestoreDb.collection("detail company")
            .document(idStock ?: "id")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Document exists, you can access its data
                    val data = doc.data // This will give you a Map<String, Any> of the document's data
                    // You can access specific fields like this:
                    name = data?.get("name") as String?
                    code = data?.get("code") as String?
                    gpm = data?.get("gpm") as String?
                    npm = data?.get("npm") as String?
                    roe = data?.get("roe") as String?
                    der = data?.get("der") as String?

                    // Update your UI elements with the retrieved data
                    binding.apply {
                        tvCompanyName.text = name
                        tvCompanyCode.text = code
                        tvGpm.text = gpm +" %"
                        tvNpm.text = npm +" %"
                        tvRoe.text = roe +" %"
                        tvDer.text = der +" %"
                    }

                    Log.d("TAG_Detail", "$idStock => $data")
                } else {
                    // Document does not exist
                    Log.d("TAG_Detail", "Document with ID $idStock does not exist.")
                }

                progresDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.w("TAG_Detail", "Error getting documents: ", exception)
                progresDialog.dismiss()
            }
    }
}