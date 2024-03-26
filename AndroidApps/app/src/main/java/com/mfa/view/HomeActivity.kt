package com.mfa.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.mfa.databinding.ActivityHomeBinding
import com.mfa.utils.PreferenceUtils


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val TAG = "HomeActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.namaUser.text = PreferenceUtils.getUsername(this)

        binding.btnKeluar.setOnClickListener {
            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener { task: Task<Void?>? ->
                    PreferenceUtils.clearData(applicationContext)
                    val intent =
                        Intent(this, SplashScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(this, "Gagal keluar Applikasi :" + e.message, Toast.LENGTH_LONG).show()
                }
        }
        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }
}