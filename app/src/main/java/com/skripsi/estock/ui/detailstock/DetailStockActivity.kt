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
import com.skripsi.estock.datasource.model.*
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.editstock.EditStockActivity
import com.skripsi.estock.ui.stockchart.StockChartActivity

class DetailStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStockBinding
    private lateinit var progresDialog: ProgressDialog

    private val firestoreDb = Firebase.firestore

    private var idStock: String? = null
    private var name: String? = null
    private var code: String? = null
    private var cgpmStock = ""
    private var cnpmStock = ""
    private var croeStock = ""
    private var cderStock = ""

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

        getData("$idStock",
            onSuccess = { detailCompany ->
                if (detailCompany != null) {
                    // Handle success, e.g., update UI with companyDetail
                    val name = detailCompany.name
                    val code = detailCompany.code
                    val gpmStock = detailCompany.gpm.gpm_stock
                    val npmStock = detailCompany.npm.npm_stock
                    val roeStock = detailCompany.roe.roe_stock
                    val derStock = detailCompany.der.der_stock



                } else {
                    // Handle case where the document does not exist
                }
            },
            onFailure = { exception ->
                // Handle the failure, e.g., show an error message
            }
        )

        binding.apply {
            btnUpdate.setSafeOnClickListener {
                val update = Intent(this@DetailStockActivity, EditStockActivity::class.java)
                update.putExtra("id", idStock)
                update.putExtra("name", name)
                update.putExtra("code", code)
                update.putExtra("gpm", cgpmStock)
                update.putExtra("npm", cnpmStock)
                update.putExtra("roe", croeStock)
                update.putExtra("der", cderStock)
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
                Toast.makeText(this, "Berhasil menghapus data saham", Toast.LENGTH_SHORT).show()
                Log.d("TAG_Hapus", "DocumentSnapshot successfully deleted!")
                finish()
                startActivity(Intent(this, StockChartActivity::class.java))
                progresDialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                Log.w("Tag_Hapus", "Gagal menghapus")
                progresDialog.dismiss()
            }
    }

    private fun getData(id: String, onSuccess: (DetailCompany?) -> Unit, onFailure: (Exception) -> Unit) {
        progresDialog.setMessage("Mengambil data")
        progresDialog.show()
        firestoreDb.collection("detail company")
            .document(id) // Use the 'id' parameter as the document ID
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val data = doc.data
                    name = data?.get("name").toString()
                    code = data?.get("code").toString()
                    val gpmMap = doc.get("gpm") as? Map<*, *>
                    val npmMap = doc.get("npm") as? Map<*, *>
                    val roeMap = doc.get("roe") as? Map<*, *>
                    val derMap = doc.get("der") as? Map<*, *>

                    if (name != null && code != null && gpmMap != null && npmMap != null && roeMap != null && derMap != null) {
                        val gpmStock = gpmMap["gpm_stock"] as? String
                        val npmStock = npmMap["npm_stock"] as? String
                        val roeStock = roeMap["roe_stock"] as? String
                        val derStock = derMap["der_stock"] as? String

                        val detailCompany = DetailCompany(
                            id,
                            name,
                            code,
                            gpm(gpmStock),
                            npm(npmStock),
                            roe(roeStock),
                            der(derStock)
                        )

                        binding.apply {
                            tvCompanyName.text = detailCompany.name
                            tvCompanyCode.text = detailCompany.code
                            tvGpm.text = detailCompany.gpm.gpm_stock
                            tvNpm.text = detailCompany.npm.npm_stock
                            tvRoe.text = detailCompany.roe.roe_stock
                            tvDer.text = detailCompany.der.der_stock

                            cgpmStock = detailCompany.gpm.gpm_stock.toString()
                            cnpmStock = detailCompany.npm.npm_stock.toString()
                            croeStock = detailCompany.roe.roe_stock.toString()
                            cderStock = detailCompany.der.der_stock.toString()
                        }
                    }
                    Toast.makeText(this, "Berhasil mendapatkan data", Toast.LENGTH_SHORT).show()

                    Log.d("TAG_Detail", "ikhsan = $id => name= $name, kode= $code, gpm= $cgpmStock, npm= $cnpmStock, roe= $croeStock, der= $cderStock")

                } else {
                    Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                    Log.d("TAG_Detail", "Document with ID $id does not exist.")
                    onSuccess(null) // Pass null to indicate document does not exist
                }

                progresDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.w("TAG_Detail", "Error getting documents: ", exception)
                onFailure(exception)
                progresDialog.dismiss()
            }
    }

}