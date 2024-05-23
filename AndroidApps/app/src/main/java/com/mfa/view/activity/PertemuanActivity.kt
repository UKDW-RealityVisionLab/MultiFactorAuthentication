package com.mfa.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mfa.Helper
import com.mfa.view.adapter.PertemuanAdapter
import com.mfa.api.response.DataJadwalItemPertemuan
import com.mfa.databinding.ActivityPertemuanBinding
import com.mfa.di.Injection
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.launch

class PertemuanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPertemuanBinding
    private lateinit var viewModel: JadwalViewModel
    private lateinit var adapter: PertemuanAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    companion object {
        const val KODEJADWAL = "jadwal"
        const val NAME = "name"
        const val RUANG= "ruang"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPertemuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(JadwalViewModel::class.java)

        adapter = PertemuanAdapter()
        setupRecyclerView()
        val getKodePertemuan = intent.getStringExtra(KODEJADWAL)
        viewModel.getPertemuan(getKodePertemuan.toString())
        viewModel.getPertemuanData.observe(this) { pertemuans ->
            if (pertemuans != null) {
                when (pertemuans) {
                    is Helper.Success -> setPertemuan(pertemuans.data.jadwal?.dataJadwal)
                    else -> {
                    }
                }
            }
        }
        getMyLocation()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvListJadwal.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvListJadwal.addItemDecoration(itemDecoration)

        // set adapter untuk rv
        binding.rvListJadwal.adapter = adapter
    }

    private fun setPertemuan(data: List<DataJadwalItemPertemuan?>?) {
        if (data != null) {
            adapter.submitList(data)
            //jumlah pertemuan
            val totalPertemuan = adapter.itemCount
            //cek total pertemuan yang ada di item
            binding.totalPertemuan.text = "Total Pertemuan: $totalPertemuan"
            Log.d("data:", "$data")
        } else {
            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // mendapatkan lokasi pengguna
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Lokasi berhasil didapatkan
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        Log.d("latitude", location.latitude.toString())
                        Log.d("longitude", location.longitude.toString())
                        // memeriksa lokasi
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
            // lokasi belum aktif, minta izin aktifkan
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
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Jika izin diberikan, dapatkan lokasi pengguna
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Lokasi berhasil didapatkan
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        Log.d("latitude", location.latitude.toString())
                        Log.d("longitude", location.longitude.toString())
                        // Panggil fungsi untuk memeriksa lokasi valid dalam korutin
                        viewModel.getKelasByKodeRuang(intent.getStringExtra(RUANG).toString())
                        viewModel.getLokasiData.observe(this) { locationData ->
                            when (locationData) {
                                is Helper.Success -> {
                                    val dataRuangItem = locationData.data?.ruang?.dataRuang?.firstOrNull()
                                    if (dataRuangItem != null) {
                                        val validLatitude = dataRuangItem.latitude as? Double
                                        val validLongitude = dataRuangItem.longitude as? Double
                                        Log.d("lokasi valid", "Latitude: $validLatitude, Longitude: $validLongitude")

                                        if (userLatitude != null && userLongitude != null && validLatitude != null && validLongitude != null) {
                                            Log.d("lokasimu", "Latitude: $userLatitude, Longitude: $userLongitude")
                                            val radius = 100.0 // radius dalam meter
                                            val isWithin100m = isWithinRadius(userLatitude!!, userLongitude!!, validLatitude, validLongitude, radius)
                                            if (isWithin100m) {
                                                Toast.makeText(
                                                    this,
                                                    "Lokasi valid",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                adapter.isvalid=true
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Lokasi tidak valid",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                adapter.isvalid=false
                                            }
                                        }
                                    }
                                }
                                is Helper.Error -> {
                                    Log.e("Error", "Failed to fetch location")
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mendapatkan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika belum, minta izin kepada pengguna
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isWithinRadius(
        userLatitude: Double,
        userLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
        radius: Double
    ): Boolean {
        //Location untuk lokasi pengguna dan lokasi target
        val userLocation = Location("").apply {
            latitude = userLatitude
            longitude = userLongitude
        }
        val targetLocation = Location("").apply {
            latitude = targetLatitude
            longitude = targetLongitude
        }

        // Hitung jarak antara lokasi pengguna dan lokasi target
        val distance = userLocation.distanceTo(targetLocation)

        // Cek apakah jarak kurang dari atau sama dengan radius
        return distance <= radius
    }
}

