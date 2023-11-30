package com.skripsi.estock.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.skripsi.estock.R
import com.skripsi.estock.databinding.ActivityForgetPasswordBinding
import com.skripsi.estock.setSafeOnClickListener

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReset.setSafeOnClickListener {
            val email:String = binding.etFillEmail.text.toString().trim()

            if (email.isEmpty()){
                binding.etEmail.error = "Email Harus diisi"
                binding.etEmail.requestFocus()
                return@setSafeOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.etEmail.error = "Email Harus diisi"
                binding.etEmail.requestFocus()
                return@setSafeOnClickListener
            }

            showResetConfirmation(email)
        }
    }

    fun showResetConfirmation(email:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Rubah Kata Sandi")
        builder.setMessage("Apakah kamu yakin akan merubah kata sandi?")

        // Set up the buttons
        builder.setPositiveButton("Hapus") { dialog, which ->
            // User clicked button
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener{
                if (it.isSuccessful){
                    Toast.makeText(this, "Cek Email untuk rubah kata sandi", Toast.LENGTH_SHORT).show()
                    Intent(this@ForgetPasswordActivity,LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }else{
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            // User clicked Cancel button
            Toast.makeText(this, "Batal Menghapus", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

}