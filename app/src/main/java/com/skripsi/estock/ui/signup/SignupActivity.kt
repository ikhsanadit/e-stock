package com.skripsi.estock.ui.signup

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
        var fullName = binding.etFillName.text.toString()
        var email = binding.etFillEmail.text.toString()
        var password = binding.etFillPassword.text.toString()

        progresDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val userUpdateProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    val user = task.result.user
                    user!!.updateProfile(userUpdateProfile)
                        .addOnCompleteListener {
                            progresDialog.dismiss()
                            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        }
                        .addOnFailureListener { error2 ->
                            Toast.makeText(this, error2.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }else{
                    progresDialog.dismiss()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun validate() {
        var etFillEmail = binding.etFillEmail
        var etFillPassword = binding.etFillPassword
        var etFillPasswordConf = binding.etFillPasswordConf
        var etFillName = binding.etFillName

        if (etFillName.text.toString().isNotEmpty()&& etFillEmail.text.toString().isNotEmpty() && etFillPassword.text.toString().isNotEmpty()){
            if (etFillPassword.text.toString() == etFillPasswordConf.text.toString()){
                processReg()
            }else{
                Toast.makeText(this, "Password Harus Sama", Toast.LENGTH_SHORT).show()
                binding.etPassword.error = "Password tidak sama"
                binding.etPasswordConf.error = "Password tidak sama"
            }
        }else{
            binding.etEmail.error = "Email tidak boleh kosong"

            binding.etPassword.error = "Password tidak boleh kosong"

            binding.etName.error = "Nama tidak boleh kosong"

            Toast.makeText(this, "Silahkan Isi Semua Data", Toast.LENGTH_SHORT).show()
        }
    }
}