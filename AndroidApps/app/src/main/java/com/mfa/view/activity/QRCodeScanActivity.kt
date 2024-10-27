package com.mfa.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.mfa.R
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.view_model.QRCodeViewModel

class QRCodeScanActivity : AppCompatActivity() {
    private lateinit var scanResultTextView: TextView
    private lateinit var scanAgainButton: Button
    private lateinit var barcodeView: DecoratedBarcodeView
    private val qrCodeViewModel: QRCodeViewModel by viewModels()

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    private val barcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                // Pause scanning
                barcodeView.pause()

                // Process the result
                val scannedResult = result.text
                val jadwal:String= intent.getStringExtra("kodeJadwal").toString()
                Log.d("data scan","$scannedResult, $jadwal ")
                val kodeJadwalRequest = KodeJadwalRequest(qrCodeData = scannedResult, kodeJadwal = jadwal)
                qrCodeViewModel.checkKodeJadwal(kodeJadwalRequest)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        // Initialize views
        barcodeView = findViewById(R.id.barcode_scanner)
        scanResultTextView = findViewById(R.id.scan_result)
        scanAgainButton = findViewById(R.id.scan_again_button)

        supportActionBar?.title = "Scan QR Code"

        // Check camera permission
        checkCameraPermission()

        // Set up the button to restart the scan
        scanAgainButton.setOnClickListener {
            findViewById<RelativeLayout>(R.id.layout_eror).visibility = View.INVISIBLE
            findViewById<DecoratedBarcodeView>(R.id.barcode_scanner).visibility = View.VISIBLE
            resumeScanning()
        }

        // Observe the ViewModel for API response
        qrCodeViewModel.kodeJadwalResponse.observe(this, Observer { result ->
            result.fold(onSuccess = { matched ->
                val message = "verify qrcode berhasil"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, FaceVerificationActivity::class.java)
                startActivity(intent)
            }, onFailure = {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                findViewById<RelativeLayout>(R.id.layout_eror).visibility = View.VISIBLE
                findViewById<DecoratedBarcodeView>(R.id.barcode_scanner).visibility=View.INVISIBLE
            })
        })
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            initializeScanner()
        }
    }

    private fun initializeScanner() {
        barcodeView.decodeContinuous(barcodeCallback)
    }

    private fun resumeScanning() {
        barcodeView.resume()
        barcodeView.decodeContinuous(barcodeCallback)
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanner()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}