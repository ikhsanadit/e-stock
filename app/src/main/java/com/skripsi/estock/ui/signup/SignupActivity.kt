package com.skripsi.estock.ui.signup

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.databinding.ActivitySignupBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.homepage.HomeActivity
import com.skripsi.estock.ui.login.LoginActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var progresDialog: ProgressDialog

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Masuk e-stock")
        progresDialog.setMessage("Silahkan Tunggu")

        binding.apply {
            btnCreate.setSafeOnClickListener {
                validate()


            }
            btnLogin.setSafeOnClickListener {
                startActivity(Intent(this@SignupActivity, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun processReg() {
        val fullName = binding.etFillName.text.toString()
        val email = binding.etFillEmail.text.toString()
        val password = binding.etFillPassword.text.toString()
        val rbAdmin = binding.rbAdmin
        val rbUser = binding.rbUser

        val selectedSuperStar: Any = when {
            rbAdmin.isChecked -> 1
            rbUser.isChecked -> 2
            else -> {
                Toast.makeText(this, "Anda harus memilih role", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val selectedRole: Int = when (selectedSuperStar) {
            is Int -> selectedSuperStar
            else -> 0
        }

        val userData = hashMapOf(
            "role" to selectedRole
        )

        Log.d("TAG_role", "value role: $selectedRole")

        progresDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.user

                    // Update display name
                    val userUpdateProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    user!!.updateProfile(userUpdateProfile)
                        .addOnCompleteListener {
                            // Save custom data to Firestore
                            val userDocRef = Firebase.firestore.collection("users").document(user.uid)
                            userDocRef.set(userData)
                                .addOnSuccessListener {
                                    progresDialog.dismiss()
                                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                }
                                .addOnFailureListener { error2 ->
                                    progresDialog.dismiss()
                                    Toast.makeText(this, error2.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { error ->
                            progresDialog.dismiss()
                            Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progresDialog.dismiss()
                    Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validate() {
        var etFillEmail = binding.etFillEmail
        var etFillPassword = binding.etFillPassword
        var etFillPasswordConf = binding.etFillPasswordConf
        var etFillName = binding.etFillName



        if (etFillName.text.toString().isNotEmpty()&& etFillEmail.text.toString().isNotEmpty() && etFillPassword.text.toString().isNotEmpty()){
            if (etFillPassword.text.toString().length >= 6){
                if (etFillPassword.text.toString() == etFillPasswordConf.text.toString()){
                    processReg()
                }else{
                    Toast.makeText(this, "Password harus sama", Toast.LENGTH_SHORT).show()
                    binding.etPassword.error = "Password tidak sama"
                    binding.etPasswordConf.error = "Password tidak sama"
                }
            }else{
                Toast.makeText(this, "Password harus lebih dari 6 huruf", Toast.LENGTH_SHORT).show()
            }

        }else{
            binding.etEmail.error = "Email tidak boleh kosong"

            binding.etPassword.error = "Password tidak boleh kosong"

            binding.etName.error = "Nama tidak boleh kosong"

            Toast.makeText(this, "Silahkan Isi Semua Data", Toast.LENGTH_SHORT).show()
        }
    }
}