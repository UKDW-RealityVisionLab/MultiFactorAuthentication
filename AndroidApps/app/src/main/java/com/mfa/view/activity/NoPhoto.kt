package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mfa.R
import com.mfa.databinding.ActivityNoPhotoBinding

class NoPhoto : AppCompatActivity() {
    private  lateinit var binding: ActivityNoPhotoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding=ActivityNoPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.btnPhoto.setOnClickListener {
            val intent = Intent(this, Simpanwajah::class.java)
//            intent.putExtra(FaceProcessorActivity.CALLER, "ubah wajah")
            startActivity(intent)
        }

    }
}