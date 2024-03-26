package com.mfa.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mfa.R
import com.mfa.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}