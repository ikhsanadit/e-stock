package com.skripsi.estock.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.skripsi.estock.databinding.ActivityProfileBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.login.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var progresDialog: ProgressDialog

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Keluar Akun")
        progresDialog.setMessage("Silahkan Tunggu")

        var fullName = binding.tvFullName
        var email= binding.tvEmail

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            fullName.text = firebaseUser.displayName
            email.text = firebaseUser.email
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