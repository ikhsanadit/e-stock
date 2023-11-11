package com.skripsi.estock.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivityProfileBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.login.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var progresDialog: ProgressDialog

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCollection = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Keluar Akun")
        progresDialog.setMessage("Silahkan Tunggu")

        val fullName = binding.tvFullName
        val email = binding.tvEmail

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            fullName.text = firebaseUser.displayName
            email.text = firebaseUser.email
        }

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
                                binding.tvRole.text = "Admin"
                            } else if (role.toInt() == 2) {
                                binding.tvRole.text = "User"
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

        binding.apply {
            btnLogout.setSafeOnClickListener {
                firebaseAuth.signOut()
                progresDialog.show()
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}