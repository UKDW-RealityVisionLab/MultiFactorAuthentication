package com.mfa.view.activity

import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.Task
import com.mfa.Helper
import com.mfa.R
import com.mfa.databinding.ActivityLocationBinding
import com.mfa.di.Injection
import com.mfa.`object`.IdJadwal.idJadwal
import com.mfa.utils.PreferenceUtils
import com.mfa.view.custom.CustomAlertDialog
import com.mfa.view.custom.LoadingDialogFragment
import com.mfa.view_model.LocationViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationActivity : AppCompatActivity() {

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CHECK_SETTINGS = 1001
    private lateinit var binding:ActivityLocationBinding

    companion object {
        const val ISVALID = "isValid"
        const val GETJADWAL = "jadwal"
        const val RUANG="ruang"
        const val NAMAPERTEMUAN="matkul"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_location)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        binding=ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya
        }
//        val matkul = intent.getStringExtra(PertemuanActivity.NAMAPERTEMUAN)
        val title = intent.getStringExtra(GETJADWAL).toString()
        supportActionBar?.title = "$title"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(LocationViewModel::class.java)
        binding.btnCheckLocation.setOnClickListener {
            val alert= CustomAlertDialog(this)
            alert.showDialog(
                title = "Cek lokasi",
                message = "Mulai cek lokasi anda??",
                onYesClick = {
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            )

        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    RESULT_OK -> {
                        Log.d("GPS_STATUS", "User enabled GPS.")
                        Toast.makeText(this, "GPS on", Toast.LENGTH_SHORT).show()
                    }
                    RESULT_CANCELED -> {
                        Log.d("GPS_STATUS", "User refused to enable GPS.")
                        Toast.makeText(this, "GPS not enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun turnOnGPS() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            Log.d("GPS_STATUS", "GPS has been turned on.")
            Toast.makeText(this, "GPS on", Toast.LENGTH_SHORT).show()
        }

        task.addOnFailureListener { exception ->
            Log.d("GPS_STATUS", "Failed to turn on GPS.")
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("GPS_STATUS", "Failed to resolve GPS settings: ${sendEx.message}")
                }
            }
        }

    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        Log.d("latitude", location.latitude.toString())
                        Log.d("longitude", location.longitude.toString())
                        val loadingDialog = LoadingDialogFragment()
                        loadingDialog.show(supportFragmentManager, "loadingDialog")
                        lifecycleScope.launch {
                            delay(3000)
                            validasiLocation()
                            loadingDialog.dismiss()
                        }
                    } else {
                        Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Gagal mendapatkan lokasi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PERMISSION", "Location permission granted.")
                turnOnGPS()
                getMyLocation()
            } else {
                Log.d("PERMISSION", "Location permission denied.")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    private fun validasiLocation() {
        val idJadwal = intent.getStringExtra(GETJADWAL).toString()
        locationViewModel.getKelasByKodeRuang(idJadwal)
        Log.d("kode jadwal pertemuan activity :", idJadwal)

//        val ruang = intent.getStringExtra(RUANG).toString()
        locationViewModel.getLokasiData.observe(this) { locationData ->
            when (locationData) {
                is Helper.Success -> {
                    val ruangResponseObject = locationData.data
                    if (ruangResponseObject != null) {
                        val validLatitude = ruangResponseObject.latitude?.toDouble()
                        val validLongitude = ruangResponseObject.longitude?.toDouble()
                        Log.d(
                            "lokasi valid",
                            "Latitude: $validLatitude, Longitude: $validLongitude"
                        )

                        if (userLatitude != null && userLongitude != null && validLatitude != null && validLongitude != null) {
                            Log.d("lokasimu", "Latitude: $userLatitude, Longitude: $userLongitude")
                            val radius = 100.0
                            val isWithin100m = isWithinRadius(
                                userLatitude!!,
                                userLongitude!!,
                                validLatitude,
                                validLongitude,
                                radius
                            )
                            if (isWithin100m) {
                                runOnUiThread {
                                    showCustomDialog("Hasil cek lokasi","Anda berada di dalam kelas. Terima kasih \natas kejujurannya. Selanjutnya mohon scan \nqr code yang dibuat oleh dosen",
                                        buttonText = "Oke, lanjut",
                                        color = R.color.green_primary,
                                        action = {
                                            val intent= Intent(this, QRCodeScanActivity::class.java)
                                            intent.putExtra("kodeJadwal","$idJadwal")
                                            startActivity(intent)
                                        }
                                    )
//                                    binding.position.text = "Anda di dalam ruang $ruang"
//                                    adapter.isvalid = true
                                }
                            } else {
                                runOnUiThread {
                                    showCustomDialog("Hasil cek lokasi","Anda berada di luar kelas. Mohon masuk\nke dalam ruangan kelas anda!!"
                                    , buttonText = "Coba lagi",
                                        color = R.color.red,
                                        action = { Log.d("hasil lokasi ", "ga") }
                                    )
//                                    binding.position.text = "Anda di luar ruang $ruang"
//                                    adapter.isvalid = false
                                }
                            }
                        }
                    } else {
                        Log.e("Error", "RUANG KOSONG")
                    }
                }

                is Helper.Error -> {
                    Log.e("Error", "GAGAL MENDAPATKAN LOKASI")
                }

//                Helper.Loading -> binding.progressBar.visibility= View.INVISIBLE
                Helper.Loading -> Log.d("load","laoding")
            }
        }
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

    private fun isWithinRadius(
        userLatitude: Double,
        userLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
        radius: Double
    ): Boolean {
        val userLocation = Location("").apply {
            latitude = userLatitude
            longitude = userLongitude
        }
        val targetLocation = Location("").apply {
            latitude = targetLatitude
            longitude = targetLongitude
        }
        val distance = userLocation.distanceTo(targetLocation)
        return distance <= radius
    }
}