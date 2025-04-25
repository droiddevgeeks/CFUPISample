package com.cashfree.cashfreetestupi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cashfree.cashfreetestupi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        addClickListener()
    }

    private fun addClickListener() {
        binding.btnScanAndPay.setOnClickListener { _ ->
            val intent = Intent(this@MainActivity, ScannerActivity::class.java)
            startActivity(intent)
        }
    }
}