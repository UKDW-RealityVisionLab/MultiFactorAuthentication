package com.mfa.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mfa.databinding.ActivityPresensiBinding

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the Scan QR button
        binding.btnScanQr.setOnClickListener {
            val intent = Intent(this, QRCodeScanActivity::class.java)
            startActivity(intent)
        }

        // Optional: Set up click listener for the Keluar button if needed
        binding.btnKeluar.setOnClickListener {
            // Handle Keluar button click
        }
    }
}
