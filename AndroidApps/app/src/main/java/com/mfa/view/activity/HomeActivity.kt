package com.mfa.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.mfa.Helper
import com.mfa.view.adapter.JadwalAdapter
import com.mfa.R
import com.mfa.api.request.EmailRequest
import com.mfa.api.response.HomeResponseItem
import com.mfa.databinding.ActivityHomeBinding
import com.mfa.di.Injection
import com.mfa.utils.PreferenceUtils
import com.mfa.`object`.Email
import com.mfa.view.custom.CustomAlertDialog
import com.mfa.view_model.JadwalViewModel
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Matikan tombol kembali
//        supportActionBar?.setHomeButtonEnabled(false) // Matikan tombol home
//        supportActionBar?.setDisplayShowHomeEnabled(false) // Pastikan ikon tidak muncul

        // Tombol Ubah Wajah
        findViewById<ImageButton>(R.id.btn_change_face).setOnClickListener {
            Toast.makeText(this, "Ubah Wajah diklik", Toast.LENGTH_SHORT).show()
        }

        // Tombol Logout
        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            val alert= CustomAlertDialog(this)
            alert.showDialog(
                title = "Logout",
                message = "Apakah anda yakin ingin keluar?",
                onYesClick = {
                    AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener { task: Task<Void?>? ->
                            PreferenceUtils.clearData(applicationContext)
//                            val intent = Intent(this, OnboardingActivity::class.java).apply {
//                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            }
//                            startActivity(intent)
                            finishAffinity()
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(this, "Gagal keluar Applikasi :" + e.message, Toast.LENGTH_LONG).show()
                        }
                }
            )
        }

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
            adapter.submitSortedList(data)
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

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.home_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.profile -> {
//                startActivity(Intent(this, UserProfileActivity::class.java))
//                return true
//            }
//
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    @SuppressLint("SimpleDateFormat")
    private fun setDate() {
        val formatter = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.ENGLISH)
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