package com.skripsi.estock.ui.homepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityHomeBinding
import com.skripsi.estock.datasource.model.DetailCompany
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.criteria.CriteriaActivity
import com.skripsi.estock.ui.homepage.adapter.HomeAdapter
import com.skripsi.estock.ui.login.LoginActivity
import com.skripsi.estock.ui.profile.ProfileActivity
import com.skripsi.estock.ui.spk.SpkActivity
import com.skripsi.estock.ui.stockchart.StockChartActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var stockAdapter: HomeAdapter

    private var list: MutableList<DetailCompany> = mutableListOf()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb = Firebase.firestore

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCollection = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

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
                                binding.cvBtnResults.visibility = View.VISIBLE
                                binding.cvBtnCriteria.visibility = View.VISIBLE
                                binding.cvBtnStock.visibility = View.VISIBLE
                            } else if (role.toInt() == 2) {
                                binding.cvBtnResults.visibility = View.VISIBLE
                                binding.cvBtnCriteria.visibility = View.GONE
                                binding.cvBtnStock.visibility = View.VISIBLE
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

        val fullName = binding.tvUserName

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            fullName.text = firebaseUser.displayName
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.apply {
            cvProfilUser.setSafeOnClickListener {
                startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                finish()
            }
            cvBtnStock.setSafeOnClickListener {
                startActivity(Intent(this@HomeActivity, StockChartActivity::class.java))
                //finish()
            }
            cvBtnCriteria.setSafeOnClickListener {
                startActivity(Intent(this@HomeActivity, CriteriaActivity::class.java))
                finish()
            }
            cvBtnResults.setSafeOnClickListener {
                startActivity(Intent(this@HomeActivity, SpkActivity::class.java))
                finish()
            }
        }

        stockAdapter = HomeAdapter(list)

        binding.rvListStock.layoutManager = LinearLayoutManager(this)
        binding.rvListStock.adapter = stockAdapter

    }

    override fun onStart() {
        super.onStart()
        getTop3()
    }

    private fun getTop3() {

        firestoreDb.collection("detail company")
            .orderBy("spk_score", Query.Direction.DESCENDING)
            .limit(3)
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
//                    list.sortWith(compareByDescending<DetailCompany> { it.gpm.gpm_spk }
//                        .thenByDescending { it.npm.npm_spk }
//                        .thenByDescending { it.roe.roe_spk }
//                        .thenBy { it.der.der_spk })

                    stockAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(applicationContext, "Data gagal di ambil!", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "getData: gagal")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mendapat data", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Error getting documents: ", exception)

            }
    }

}