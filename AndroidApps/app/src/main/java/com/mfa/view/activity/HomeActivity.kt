package com.mfa.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.mfa.Helper
import com.mfa.view.adapter.JadwalAdapter
import com.mfa.R
import com.mfa.api.request.EmailRequest
import com.mfa.api.response.HomeResponseItem
import com.mfa.databinding.ActivityHomeBinding
import com.mfa.di.Injection
import com.mfa.utils.PreferenceUtils
import com.mfa.`object`.Email
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: JadwalViewModel
    private lateinit var adapter: JadwalAdapter
    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        const val TAG = "HomeActivity"
        const val NAME = "name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.namaUser.text = PreferenceUtils.getUsername(this)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(JadwalViewModel::class.java)

        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        adapter = JadwalAdapter()
        setupRecyclerView()

        val dataEmail = EmailRequest(Email.email)
        profileViewModel.getProfile(dataEmail)

        Log.d("email", Email.email.toString())
    }

    //    this is response
    fun getNama(callback: (String?) -> Unit) {
        profileViewModel.getData.observe(this) {
            val name = it.nama
            callback(name)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvJadwal.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvJadwal.addItemDecoration(itemDecoration)

        binding.rvJadwal.adapter = adapter

        viewModel.getJadwal()
        viewModel.getJadwalLiveData.observe(this) {
            if (it != null) {
                when (it) {
                    is Helper.Success -> setJadwal(it.data)
                    else -> {
                        Toast.makeText(this,"Jadwal tidak ditemuka",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setJadwal(data: List<HomeResponseItem?>?) {
        if (data != null) {
            adapter.submitList(data)
            Log.d("data Home:", "$data")
        } else {
            // Handle null data if needed
            Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        getNama {
            binding.namaUser.text = it ?: "unregis user"
        }
        setDate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDate() {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = Date()
        val current = formatter.format(date)

        binding.tglCard.text = current.toString()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        AlertDialog.Builder(this).apply {
            setTitle("Keluar")
            setMessage("Anda yakin ingin keluar dari aplikasi?")
            setPositiveButton("Ya") { _, _ ->
                finishAffinity()
            }
            setNegativeButton("Tidak", null)
            create()
            show()
        }
    }
}