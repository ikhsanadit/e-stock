package com.skripsi.estock.ui.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.skripsi.estock.databinding.ActivityLoginBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.homepage.HomeActivity
import com.skripsi.estock.ui.signup.SignupActivity


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var progresDialog: ProgressDialog

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this,HomeActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Masuk e-stock")
        progresDialog.setMessage("Silahkan Tunggu")

        binding.apply {
            btnLogin.setSafeOnClickListener {
                validateLogin()
            }
            btnSignup.setSafeOnClickListener {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }
            btnForget.setSafeOnClickListener {
                startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
            }
        }
    }

    private fun validateLogin() {
        val email = binding.etFillEmail.text.toString()
        val password = binding.etFillPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            processLogin()
        }else{
            if (email.isEmpty()&& password.isEmpty()){
                binding.etEmail.error = "Email tidak boleh kosong"

                binding.etPassword.error = "Password tidak boleh kosong"
            }
            Toast.makeText(this, "Silahkan isi email dan password terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processLogin() {
        val email = binding.etFillEmail.text.toString()
        val password = binding.etFillPassword.text.toString()

        progresDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progresDialog.dismiss()
            }
    }


}