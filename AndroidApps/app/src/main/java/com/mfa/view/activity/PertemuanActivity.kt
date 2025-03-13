package com.mfa.view.activity

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.mfa.Helper
import com.mfa.api.request.EmailRequest
import com.mfa.view.adapter.PertemuanAdapter
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.databinding.ActivityPertemuanBinding
import com.mfa.di.Injection
import com.mfa.`object`.Email
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.PertemuanViewModel
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory

class PertemuanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPertemuanBinding
    private lateinit var viewModel: JadwalViewModel
    private lateinit var pertemuanViewModel: PertemuanViewModel

    private lateinit var adapter: PertemuanAdapter
    private lateinit var profileViewModel: ProfileViewModel



    companion object {
        const val KODEKELAS = "kode_kelas"
        const val NAMAPERTEMUAN = "pertemuan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPertemuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        val matkul = intent.getStringExtra(NAMAPERTEMUAN)

        supportActionBar?.title = "$matkul"
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya
        }


        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(JadwalViewModel::class.java)

        pertemuanViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(PertemuanViewModel::class.java)

        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        adapter = PertemuanAdapter()
        setupRecyclerView()

        val dataEmail = EmailRequest(Email.email)

        profileViewModel.getProfile(dataEmail)

        profileViewModel.getData.observe(this) {
            val name = it.nama
            val nim = it.nim
            val email = it.email
            Log.d("email pertemuan", "$email $dataEmail")
        }

        val kodeKelas = intent.getIntExtra(KODEKELAS, 0)

        Log.d("pertemuan menerima kode ", kodeKelas.toString())


        pertemuanViewModel.getPertemuan(kodeKelas)
        pertemuanViewModel.getPertemuanData.observe(this) { pertemuans ->
            if (pertemuans != null) {
                when (pertemuans) {
                    is Helper.Success -> setPertemuan(pertemuans.data)
                    is Helper.Error -> {
                        Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }

                    Helper.Loading -> binding.progressBar.visibility=View.INVISIBLE
                }
            }
        }
    }



    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvListJadwal.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvListJadwal.addItemDecoration(itemDecoration)
        binding.rvListJadwal.adapter = adapter
    }

    private fun setPertemuan(data: List<PertemuanResponseItem>?) {
        if (data != null) {
            adapter.submitList(data)
            val totalPertemuan = adapter.itemCount
            binding.totalPertemuan.text = "Total Pertemuan: $totalPertemuan"
            Log.d("data pertemuan:", data.toString())
        } else {
            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show()
        }
    }
}