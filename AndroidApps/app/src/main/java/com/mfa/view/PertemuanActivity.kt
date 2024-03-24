package com.mfa.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mfa.databinding.ActivityPertemuanBinding

class PertemuanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPertemuanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPertemuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}