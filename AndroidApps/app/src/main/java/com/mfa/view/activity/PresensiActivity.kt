package com.mfa.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mfa.Helper
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.StatusReq
import com.mfa.databinding.ActivityPresensiBinding
import com.mfa.di.Injection
import com.mfa.`object`.Email
import com.mfa.`object`.IdJadwal
import com.mfa.`object`.StatusMhs
import com.mfa.view.adapter.PertemuanAdapter
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.LocationViewModel
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.launch

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding
    private lateinit var viewModel: JadwalViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var adapter: PertemuanAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var profileViewModel: ProfileViewModel

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    companion object {
        const val ISVALID = "isValid"
        const val GETJADWAL = "jadwal"
        const val RUANG="ruang"
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
        locationViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(LocationViewModel::class.java)
        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        adapter = PertemuanAdapter()

        val idJadwal = intent.getStringExtra(GETJADWAL).toString()
        supportActionBar?.title="Presensi $idJadwal"
        Log.d("id jadwal untuk getjadwal", idJadwal)

        getMyLocation()

        // Setup onClickListener for "Scan QR" button
        binding.btnScanQr.setOnClickListener {
            if (adapter.isvalid == true) {
                // User is inside the classroom, allow QR code scanning
                val intent = Intent(this, QRCodeScanActivity::class.java)
                intent.putExtra("kodeJadwal","$idJadwal")
                startActivity(intent)
            } else {
                alertDialog(title="Pemberitahuan", text = "Anda berada di luar kelas\nsilahkan masuk kelas untuk presensi")
            }
        }

    }

    fun alertDialog(title:String,text:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(text)

        builder.setPositiveButton("Oke") { dialog, which ->
            onResume()
        }

//        builder.setNegativeButton(android.R.string.no) { dialog, which ->
//            Toast.makeText(applicationContext,
//                android.R.string.no, Toast.LENGTH_SHORT).show()
//        }
//
//        builder.setNeutralButton("Maybe") { dialog, which ->
//            Toast.makeText(applicationContext,
//                "Maybe", Toast.LENGTH_SHORT).show()
//        }
        builder.show()
    }


    override fun onResume() {
        super.onResume()
        checkStatus()
    }

    private fun checkStatus() {
        val idJadwal = intent.getStringExtra(GETJADWAL).toString()
        IdJadwal.idJadwal = idJadwal
        val dataEmail = EmailRequest(Email.email)
        profileViewModel.getProfile(dataEmail)
        profileViewModel.getData.observe(this) {
            val nim = it.nim
            val data = StatusReq(idJadwal, nim)
            profileViewModel.getStatus(data)
            profileViewModel.getDataStatus.observe(this) { status ->

                if (status == true) {
                    StatusMhs.statusMhs = true
                    Log.d("status", " $data ${StatusMhs.statusMhs} ${it.email}")
                    if (StatusMhs.statusMhs == true) {
                        binding.status.text = "Status: Hadir"
                        binding.btnScanQr.visibility = View.GONE
                        binding.btnBackHome.visibility = View.VISIBLE
                        binding.btnBackHome.setOnClickListener {
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                    }
                } else if (status == false) {
                    StatusMhs.statusMhs = false
                    if (StatusMhs.statusMhs == false) {
                        binding.status.text = "Status: Alpha"
                    }
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
                        lifecycleScope.launch {
                            validasiLocation()
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
                getMyLocation()
            }
        }

    private fun validasiLocation() {
        val idJadwal = intent.getStringExtra(GETJADWAL).toString()
        locationViewModel.getKelasByKodeRuang(idJadwal)
        Log.d("kode jadwal pertemuan activity :", idJadwal)
        val ruang = intent.getStringExtra(RUANG).toString()
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
                                    Toast.makeText(
                                        this,
                                        "Lokasi valid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.position.text = "Anda di dalam ruang $ruang"
                                    adapter.isvalid = true
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Lokasi tidak valid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.position.text = "Anda di luar ruang $ruang"
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

                Helper.Loading -> binding.progressBar.visibility=View.INVISIBLE
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
