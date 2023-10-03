package com.skripsi.estock.ui.homepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.skripsi.estock.databinding.ActivityHomeBinding
import com.skripsi.estock.setSafeOnClickListener
import com.skripsi.estock.ui.criteria.CriteriaActivity
import com.skripsi.estock.ui.login.LoginActivity
import com.skripsi.estock.ui.profile.ProfileActivity
import com.skripsi.estock.ui.stockchart.StockChartActivity
import dagger.hilt.android.AndroidEntryPoint

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        var fullName = binding.tvUserName

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
                finish()
            }
            cvBtnCriteria.setSafeOnClickListener {
                startActivity(Intent(this@HomeActivity, CriteriaActivity::class.java))
                finish()
            }
            cvBtnResults.setSafeOnClickListener {
                //startActivity(Intent(this@HomeActivity, ))
            }
        }

    }
}