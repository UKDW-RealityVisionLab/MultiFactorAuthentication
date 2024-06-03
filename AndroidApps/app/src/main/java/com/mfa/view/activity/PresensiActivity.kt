package com.mfa.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mfa.Helper
import com.mfa.R
import com.mfa.databinding.ActivityPresensiBinding
import com.mfa.di.Injection
import com.mfa.view.adapter.PertemuanAdapter
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.launch

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding
    private lateinit var viewModel: JadwalViewModel
    private lateinit var adapter: PertemuanAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    companion object {
        const val ISVALID = "isValid"
        const val GETRUANG = "r"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(JadwalViewModel::class.java)

        adapter = PertemuanAdapter()

        val idJadwal = intent.getStringExtra(GETRUANG).toString()
        Log.d("id jadwal untuk getjadwal", idJadwal)

        getMyLocation()

        // Setup onClickListener for "Scan QR" button
        binding.btnScanQr.setOnClickListener {
            if (adapter.isvalid == true) {
                // User is inside the classroom, allow QR code scanning
                val intent = Intent(this, QRCodeScanActivity::class.java)
                startActivity(intent)
            } else {
                // User is not inside the classroom, show a message or disable the button
                Toast.makeText(this, "Anda harus berada di dalam kelas untuk melakukan pemindaian QR.", Toast.LENGTH_SHORT).show()
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
                        lifecycleScope.launch {
                            validasiLocation()
                        }
                    } else {
                        Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mendapatkan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
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
                getMyLocation()
            }
        }

    private fun validasiLocation() {
        val idJadwal = intent.getStringExtra(GETRUANG).toString()
        viewModel.getKelasByKodeRuang(idJadwal)
        Log.d("kode jadwal pertemuan activity :", idJadwal)

        viewModel.getLokasiData.observe(this) { locationData ->
            when (locationData) {
                is Helper.Success -> {
                    val ruangResponseObject = locationData.data
                    if (ruangResponseObject != null) {
                        val validLatitude = ruangResponseObject.latitude?.toDouble()
                        val validLongitude = ruangResponseObject.longitude?.toDouble()
                        Log.d("lokasi valid", "Latitude: $validLatitude, Longitude: $validLongitude")

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
                                    Toast.makeText(
                                        this,
                                        "Lokasi valid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.position.text = "Anda di dalam kelas"
                                    adapter.isvalid = true
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Lokasi tidak valid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.position.text = "Anda di luar kelas"
                                    adapter.isvalid = false
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
            }
        }
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
