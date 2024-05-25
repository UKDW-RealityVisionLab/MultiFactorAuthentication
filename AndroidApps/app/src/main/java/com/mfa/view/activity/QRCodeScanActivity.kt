package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import com.mfa.R
import org.json.JSONObject

class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var scanResultTextView: TextView
    private lateinit var scanAgainButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        scanResultTextView = findViewById(R.id.scan_result)
        scanAgainButton = findViewById(R.id.scan_again_button)

        startQRCodeScanner()

        scanAgainButton.setOnClickListener {
            startQRCodeScanner()
        }
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Scan a QR code")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedResult = result.contents
                scanResultTextView.text = "Scanned: $scannedResult"
                try {
                    val params = parseQRCodeContent(scannedResult)
                    if (params != null) {
                        makeAPICall(params["kode_jadwal"]!!, params["nim_mahasiswa"]!!)
                    } else {
                        Toast.makeText(this, "Invalid QR code content", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Invalid QR code content: $scannedResult")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Invalid QR code format", e)
                }
            } else {
                scanResultTextView.text = "Scan canceled"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun parseQRCodeContent(scannedResult: String): Map<String, String>? {
        val pairs = scannedResult.split("&")
        val map = mutableMapOf<String, String>()
        for (pair in pairs) {
            val keyValue = pair.split("=")
            if (keyValue.size == 2) {
                map[keyValue[0].trim()] = keyValue[1].trim()
            }
        }
        return if (map.size == 2 && map.containsKey("kode_jadwal") && map.containsKey("nim_mahasiswa")) {
            map
        } else {
            null
        }
    }

    private fun makeAPICall(kodeJadwal: String, nimMahasiswa: String) {
        try {
            val url = "http://172.16.50.227:3000/updateHadirStatus"
            val jsonObject = JSONObject().apply {
                put("kode_jadwal", kodeJadwal)
                put("nim_mahasiswa", nimMahasiswa)
                put("hadir", 1)
            }

            // Log the request details for comparison
            Log.d(TAG, "Making POST request to: $url with payload: $jsonObject")

            val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
                { response ->
                    Toast.makeText(this, "Attendance updated successfully", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    val errorMessage = if (error.networkResponse != null) {
                        "Failed to update attendance: ${error.message} - ${String(error.networkResponse.data)}"
                    } else {
                        "Failed to update attendance: ${error.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, errorMessage)
                }
            )

            Volley.newRequestQueue(this).add(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating attendance", e)
        }
    }

    companion object {
        private const val TAG = "QRCodeScanActivity"
    }
}
