package com.mfa.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.mfa.R

class QRCodeScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

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
                Toast.makeText(this, "Scanned: $scannedResult", Toast.LENGTH_LONG).show()
                // You can also start another activity or process the result as needed
            } else {
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
