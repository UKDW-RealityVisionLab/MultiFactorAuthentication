package com.mfa.view.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.mfa.R
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.databinding.ActivityQrCodeScanBinding
import com.mfa.view.activity.LocationActivity.Companion.GETJADWAL
import com.mfa.view.custom.LoadingDialogFragment
import com.mfa.view_model.QRCodeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QRCodeScanActivity : AppCompatActivity() {
    private lateinit var scanResultTextView: TextView
    private lateinit var scanAgainButton: Button
    private lateinit var barcodeView: DecoratedBarcodeView
    private val qrCodeViewModel: QRCodeViewModel by viewModels()
    private lateinit var binding:ActivityQrCodeScanBinding

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
//        setContentView(R.layout.activity_qr_code_scan)
        binding= ActivityQrCodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        barcodeView = findViewById(R.id.barcode_scanner)
        scanResultTextView = findViewById(R.id.scan_result)
        scanAgainButton = findViewById(R.id.scan_again_button)

        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Scan qr code"

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
            val loadingDialog = LoadingDialogFragment()
            loadingDialog.show(supportFragmentManager, "loadingDialog") // Tampilkan loading

            result.fold(onSuccess = { matched ->
                lifecycleScope.launch {
                    delay(3000) //  Tunggu 3 detik
                    loadingDialog.dismiss() // Sembunyikan loading setelah selesai

                    showCustomDialog(
                        "Hasil scan QR Code",
                        "Selamat, QR code yang Anda scan berhasil dan terbukti cocok. Satu langkah lagi untuk menuju keberhasilan presensi.",
                        "Siap, Lanjut", color = R.color.green_primary
                    ) {
                        val intent = Intent(this@QRCodeScanActivity, FaceProcessorActivity::class.java)
                        startActivity(intent)
                    }
                }
            }, onFailure = {
                lifecycleScope.launch {
                    delay(3000)
                    loadingDialog.dismiss()

                    showCustomDialog(
                        title = "Hasil scan QR Code",
                        message = "Hasil QR code yang Anda scan tidak cocok dengan yang dibuat dosen di kelas Anda saat ini atau telah expired",
                        buttonText = "Coba Lagi",
                        color = R.color.red,
                        action = {
                            onResume()
                        }
                    )
                }

            })
        })

        onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                showCustomDialog(
                    title = "Pemberitahuan",
                    message = "Mohon selesaikan proses presensi",
                    buttonText = "Oke",
                    color = R.color.green_primary
                ){
                    onResume()
                }
            }
        })
    }


    private fun showCustomDialog(
        title: String,
        message: String,
        buttonText: String,
        color: Int, // warna tombol
        action: () -> Unit
    ) {
        val dialog = Dialog(this).apply {
            setCancelable(false)
            setContentView(R.layout.custom_alert_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<TextView>(R.id.tvTitle).text = title
            findViewById<TextView>(R.id.tvMessage).text = message
            findViewById<Button>(R.id.btnConfirm).apply {
                text = buttonText
                setTextColor(Color.WHITE) // Warna teks
//                setBackgroundColor(color)
                val buttonColor = ContextCompat.getColor(context, color)
                backgroundTintList = ColorStateList.valueOf(buttonColor) // Warna latar
                setOnClickListener {
                    action()
                    dismiss()
                }
            }
        }
        dialog.show()
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