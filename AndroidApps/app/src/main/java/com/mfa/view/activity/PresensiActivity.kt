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
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mfa.Helper
import com.mfa.R
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.StatusReq
import com.mfa.databinding.ActivityPresensiBinding
import com.mfa.di.Injection
import com.mfa.`object`.Email
import com.mfa.`object`.IdJadwal
import com.mfa.`object`.StatusMhs
import com.mfa.view.activity.PertemuanActivity.Companion
import com.mfa.view.activity.PertemuanActivity.Companion.NAMAPERTEMUAN
import com.mfa.view.activity.PresensiActivity.Companion.GETJADWAL
import com.mfa.view.adapter.PertemuanAdapter
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.LocationViewModel
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.launch

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding
    private lateinit var viewModel: JadwalViewModel

    private lateinit var adapter: PertemuanAdapter

    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        const val ISVALID = "isValid"
        const val GETJADWAL = "jadwal"
        const val RUANG="ruang"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(JadwalViewModel::class.java)

        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        adapter = PertemuanAdapter()

        val idJadwal = intent.getStringExtra(GETJADWAL).toString()

        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
//        val matkul = intent.getStringExtra(PertemuanActivity.NAMAPERTEMUAN)

        supportActionBar?.title = "$idJadwal"
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya
        }
//        supportActionBar?.title="Presensi $idJadwal"
//        Log.d("id jadwal untuk getjadwal", idJadwal)

//        getMyLocation()

        // Setup onClickListener for "Scan QR" button
        binding.btnPresensi.setOnClickListener {
//            if (adapter.isvalid == true) {
                // User is inside the classroom, allow QR code scanning
                val intent = Intent(this, LocationActivity::class.java)
                val jadwal= idJadwal.substringBefore("grup").trim()
                intent.putExtra(LocationActivity.GETJADWAL,"$jadwal")
                startActivity(intent)
//            } else {
//                alertDialog(title="Pemberitahuan", text = "Anda berada di luar kelas\nsilahkan masuk kelas untuk presensi")
//            }
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
        var idJadwal = intent.getStringExtra(GETJADWAL).toString()
        idJadwal= idJadwal.substringBefore("grup").trim()
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
                        binding.status.text = "Hadir"
                        binding.status.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.btnPresensi.visibility = View.GONE
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
                        binding.status.text = "Alpha"
                    }
                }
            }
        }

    }


}
