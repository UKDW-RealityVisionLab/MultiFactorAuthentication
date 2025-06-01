package com.mfa.view.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding
    private lateinit var viewModel: JadwalViewModel

    private lateinit var adapter: PertemuanAdapter

    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        const val ISVALID = "isValid"
        const val GETJADWAL = "jadwal"
        const val RUANG="ruang"
        const val WAKTU="waktu"
        const val TANGGAL="tanggal"
        const val DOSEN="dosen"
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
        val waktu = intent.getStringExtra(WAKTU).toString()
        val dosen= intent.getStringExtra(DOSEN).toString()
        val tanggal=intent.getStringExtra(TANGGAL).toString()
        val ruang=intent.getStringExtra(RUANG).toString()

        binding.waktu.text="Waktu      : $waktu"
        binding.tgl.text="Tanggal   : $tanggal"
        binding.ruang.text="Ruang      : $ruang"
        binding.jadwal.text=idJadwal
        binding.dosen.text="Dosen      : $dosen"

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
//            val waktuText = binding.waktu.text.toString()
//            val tglText = binding.tgl.text.toString()
//            cekPresensi(tglText,waktuText)

                val intent = Intent(this, LocationActivity::class.java)
                val jadwal= idJadwal.substringBefore("grup").trim()
                intent.putExtra(LocationActivity.GETJADWAL,"$jadwal")
                startActivity(intent)


        }

    }
    private fun cekPresensi(tglText: String, waktuText: String) {
        val idJadwal = intent.getStringExtra(GETJADWAL).toString()
        try {
            // Ambil tanggal dari binding text
            val tanggal = tglText.substringAfter(":").trim()
            val waktuStart = waktuText.substringAfter(":").substringBefore("-").trim()
            val waktuEnd = waktuText.substringAfter("-").trim()

            // Format
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val tanggalDariTeks = LocalDate.parse(tanggal, dateFormatter)
            val waktuMulai = LocalTime.parse(waktuStart, timeFormatter)
            val waktuSelesai = LocalTime.parse(waktuEnd, timeFormatter)

            // Sekarang
            val tanggalSekarang = LocalDate.now()
            val waktuSekarang = LocalTime.now()
            Log.d("waktu saat ini, waktu mulai, selesai", "$waktuSekarang $waktuStart $waktuEnd")
            if (tanggalSekarang != tanggalDariTeks) {
                Log.d("Presensi", "Tidak sesuai: tanggal beda")
                showCustomDialog("Pemberitahuan","Bukan sesi anda!!"
                    , buttonText = "Oke",
                    color = R.color.red,
                    action = {
                    }
                )
            } else if (waktuSekarang.isBefore(waktuMulai)) {
                Log.d("Presensi", "Tidak sesuai: belum masuk waktu presensi")
                showCustomDialog("Pemberitahuan","Bukan sesi anda!!"
                    , buttonText = "Oke",
                    color = R.color.red,
                    action = {
                    }
                )
            } else if (waktuSekarang.isAfter(waktuSelesai)) {
                Log.d("Presensi", "Tidak sesuai: sudah lewat waktu presensi")
                showCustomDialog("Pemberitahuan","Bukan sesi anda!!"
                    , buttonText = "Oke",
                    color = R.color.red,
                    action = {
                    }
                )
            } else {
                Log.d("Presensi", "Presensi valid! Bisa lanjut")
                val intent = Intent(this, LocationActivity::class.java)
                val jadwal= idJadwal.substringBefore("grup").trim()
                intent.putExtra(LocationActivity.GETJADWAL,"$jadwal")
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e("Presensi", "Gagal parsing waktu/tanggal: ${e.message}")
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
