package com.skripsi.estock.ui.criteria

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skripsi.estock.databinding.ActivityCriteriaBinding

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}