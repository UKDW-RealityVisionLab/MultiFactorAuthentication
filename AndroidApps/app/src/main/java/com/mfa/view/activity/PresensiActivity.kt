// PresensiActivity.kt
package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.mfa.R
import com.mfa.api.retrofit.ApiConfig
import com.mfa.databinding.ActivityPresensiBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding

    companion object {
        const val ISVALID = "isValid"
        const val TAG = "PresensiActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isValidLocation = intent.getBooleanExtra(ISVALID, false)
        binding.position.text = if (isValidLocation) {
            getString(R.string.andadi_dalam_kelas)
        } else {
            getString(R.string.andadi_luar_kelas)
        }

        binding.btnScanQr.setOnClickListener {
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
                handleQRCodeResult(result.contents)
            } else {
                showToast("Scan canceled")
            }
        }
    }

    private fun handleQRCodeResult(scannedResult: String) {
        try {
            val params = parseQRCodeContent(scannedResult)
            if (params != null) {
                updatePresensi(params["kode_jadwal"], params["nim_mahasiswa"])
            } else {
                showToast("Invalid QR code content")
                Log.e(TAG, "Invalid QR code content: $scannedResult")
            }
        } catch (e: Exception) {
            showToast("Invalid QR code format")
            Log.e(TAG, "Invalid QR code format", e)
        }
    }

    private fun parseQRCodeContent(scannedResult: String): Map<String, String>? {
        val pairs = scannedResult.split("&")
        val map = mutableMapOf<String, String>()
        for (pair in pairs) {
            val keyValue = pair.split("=")
            if (keyValue.size == 2) {
                map[keyValue[0]] = keyValue[1]
            }
        }
        return if (map.size == 2 && map.containsKey("kode_jadwal") && map.containsKey("nim_mahasiswa")) {
            map
        } else {
            null
        }
    }

    private fun updatePresensi(kodeJadwal: String?, nimMahasiswa: String?) {
        if (kodeJadwal != null && nimMahasiswa != null) {
            val apiService = ApiConfig.getApiService()
            val call = apiService.updateHadirStatus(kodeJadwal, nimMahasiswa, "1")
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        showToast("Attendance updated successfully")
                    } else {
                        showToast("Failed to update attendance")
                        Log.e(TAG, "Failed to update attendance: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    showToast("Network error occurred")
                    Log.e(TAG, "Network error", t)
                }
            })
        } else {
            showToast("Invalid QR code content")
            Log.e(TAG, "Invalid QR code content: kodeJadwal=$kodeJadwal, nimMahasiswa=$nimMahasiswa")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

