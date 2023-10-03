package com.skripsi.estock.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.skripsi.estock.databinding.ActivitySplashBinding
import com.skripsi.estock.ui.homepage.HomeActivity
import com.skripsi.estock.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

//    private var firebaseAuth = FirebaseAuth.getInstance()
//
//    override fun onStart() {
//        super.onStart()
//        if (firebaseAuth.currentUser != null){
//            startActivity(Intent(this, HomeActivity::class.java))
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}