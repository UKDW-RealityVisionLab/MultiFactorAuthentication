package com.mfa.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.mfa.R

class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var scanResultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        scanResultTextView = findViewById(R.id.scan_result)

        // Initialize the QR code scanner
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false) // Allow orientation rotation
        integrator.setPrompt("Scan a QR code")
        integrator.setBeepEnabled(true)
        integrator.initiateScan() // Start scanning
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle scan result
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // QR code scanned successfully, handle the result
                val scannedResult = result.contents
                scanResultTextView.text = "Scanned: $scannedResult"
            } else {
                scanResultTextView.text = "Scan canceled"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
