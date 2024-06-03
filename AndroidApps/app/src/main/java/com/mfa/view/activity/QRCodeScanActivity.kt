package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.zxing.integration.android.IntentIntegrator
import com.mfa.R
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.viewmodel.QRCodeViewModel

class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var scanResultTextView: TextView
    private lateinit var scanAgainButton: Button
    private val qrCodeViewModel: QRCodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        scanResultTextView = findViewById(R.id.scan_result)
        scanAgainButton = findViewById(R.id.scan_again_button)

        // Initialize the QR code scanner
        startQRCodeScanner()

        // Set up the button to restart the scan
        scanAgainButton.setOnClickListener {
            startQRCodeScanner()
        }

        // Observe the ViewModel for API response
        qrCodeViewModel.kodeJadwalResponse.observe(this, Observer { result ->
            result.fold(onSuccess = { matched ->
                if (matched) {
                    Toast.makeText(this, "QR Code matched!", Toast.LENGTH_SHORT).show()
                    // Start the face scanner activity
                    val intent = Intent(this, FaceScannerActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "QR Code matched!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FaceScannerActivity::class.java)
                    startActivity(intent)
                }
                scanResultTextView.text = if (matched) "QR Code matched!" else "QR Code matched!"
            }, onFailure = {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                scanResultTextView.text = "Error: ${it.message}"
            })
        })
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true) // Allow orientation rotation
        integrator.setPrompt("Scan a QR code")
        integrator.setBeepEnabled(true)
        integrator.initiateScan() // Start scanning
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle scan result
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // QR code scanned successfully, handle the result
                val scannedResult = result.contents
                val kodeJadwal = extractKodeJadwal(scannedResult)
                val kodeJadwalRequest = KodeJadwalRequest(qrCodeData = scannedResult, kodeJadwal = kodeJadwal)
                scanResultTextView.text = "Scanned: $kodeJadwalRequest"
                qrCodeViewModel.checkKodeJadwal(kodeJadwalRequest)
            } else {
                scanResultTextView.text = "Scan canceled"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun extractKodeJadwal(qrCodeData: String): String {
        // Adjust this method to extract the correct part of the QR code data
        // This is a placeholder, adjust according to your QR code structure
        val parts = qrCodeData.split(" ")
        return if (parts.isNotEmpty()) {
            parts[0]+" "+parts[1]
        } else {
            ""
        }
    }

}
