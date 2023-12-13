package com.skripsi.estock.ui.criteria

import android.R
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityCriteriaBinding
import com.skripsi.estock.databinding.DialogCriteriaBinding
import com.skripsi.estock.datasource.model.DetailCompany
import com.skripsi.estock.datasource.model.WeightCriteria
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.spk.SpkActivity
import kotlin.math.pow
import kotlin.math.sqrt

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCollection = Firebase.firestore.collection("users")
    private val firestoreDb = Firebase.firestore

    private var listWeight = mutableListOf<String>()
    private var weightC1 = ""
    private var weightC2 = ""
    private var weightC3 = ""
    private var weightC4 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.tvEdit.setSafeOnClickListener {
            showCustomDialog()
        }
        weightCriteria()
    }

    private fun weightCriteria() {
        firestoreDb.collection("criteria weight")
            .document("mK1XufxOAEdhG8jxVYQq")
            .get()
            .addOnCompleteListener { task ->
                Log.d("TAG task", "isi task: $task ")
                if (task.isSuccessful) {
                    val document = task.result!!
                    val wGpm = document.getDouble("gpm_weight")
                    val wNpm = document.getDouble("npm_weight")
                    val wRoe = document.getDouble("roe_weight")
                    val wDer = document.getDouble("der_weight")

                    if ( wGpm != null && wNpm != null && wRoe != null && wDer != null) {


                        binding.apply {
                            tvWGpm.text = wGpm.toString()
                            tvWNpm.text = wNpm.toString()
                            tvWRoe.text = wRoe.toString()
                            tvWDer.text = wDer.toString()
                        }
                    }

                } else {
                    Toast.makeText(applicationContext, "Data gagal di ambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                }

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
                firestoreDb.collection("criteria weight")
                    .document("mK1XufxOAEdhG8jxVYQq")
                    .update(criteriaWeight as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Berhasil merubah bobot", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, CriteriaActivity::class.java))
                        customDialog.dismiss()
                        finish()
                        recreate()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal merubah bobot", Toast.LENGTH_SHORT).show()
                        Log.w("TAG", "Error updating document", e)
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