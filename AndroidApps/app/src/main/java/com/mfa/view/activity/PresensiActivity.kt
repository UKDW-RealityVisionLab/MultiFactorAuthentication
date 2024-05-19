package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mfa.R
import com.mfa.databinding.ActivityPresensiBinding

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding

    companion object {
        const val ISVALID = "isValid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isValidLocation = intent.getBooleanExtra(ISVALID, false)
        if (isValidLocation) {
            binding.position.text = getString(R.string.andadi_dalam_kelas)
        } else {
            binding.position.text = getString(R.string.andadi_luar_kelas)
        }

        binding.btnScanQr.setOnClickListener {
            val intent = Intent(this, QRCodeScanActivity::class.java)
            startActivity(intent)
        }
    }
}
