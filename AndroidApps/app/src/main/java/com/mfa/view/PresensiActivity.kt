package com.mfa.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mfa.R
import com.mfa.databinding.ActivityPresensiBinding

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPresensiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}