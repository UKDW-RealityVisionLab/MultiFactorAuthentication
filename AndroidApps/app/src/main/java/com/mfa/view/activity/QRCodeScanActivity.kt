package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val qrCodeScannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (intentResult != null) {
            if (intentResult.contents != null) {
                // QR code scanned successfully, handle the result
                val scannedResult = intentResult.contents
                val kodeJadwal = extractKodeJadwal(scannedResult)
                val kodeJadwalRequest = KodeJadwalRequest(qrCodeData = scannedResult, kodeJadwal = kodeJadwal)
                scanResultTextView.text = "Scanned: $kodeJadwalRequest"
                qrCodeViewModel.checkKodeJadwal(kodeJadwalRequest)
            } else {
                scanResultTextView.text = "Scan canceled"
            }
        }
    }

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
                val message = "QR Code matched!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                scanResultTextView.text = message

                val intent = Intent(this, FaceVerificationActivity::class.java)
                startActivity(intent)

            }, onFailure = {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                scanResultTextView.text = "Error: ${it.message}"
            })
        })
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true) // Lock orientation
        integrator.setPrompt("Scan a QR code")
        integrator.setBeepEnabled(true)
        qrCodeScannerLauncher.launch(integrator.createScanIntent())
    }

    private fun extractKodeJadwal(qrCodeData: String): String {
        // Extract the relevant part of the QR code data
        // Assuming the format is: "PROGWEB 1 6/3/2024, 10:57:02"
        val parts = qrCodeData.split(" ")
        return if (parts.size >= 2) {
            "${parts[0]} ${parts[1]}"
        } else {
            ""
        }
    }
}
