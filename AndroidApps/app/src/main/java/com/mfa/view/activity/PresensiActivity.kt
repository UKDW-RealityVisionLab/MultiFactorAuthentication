package com.mfa.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mfa.R
import com.mfa.databinding.ActivityPresensiBinding
class PresensiActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPresensiBinding

    companion object {
        const val ISVALID ="isValid"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val isValidLocation = intent.getBooleanExtra(ISVALID, true)
        val isValidLocation = intent.getBooleanExtra(ISVALID, false)
        Log.d("PresensiActivity", "isValidLocation: $isValidLocation") // Add this line for debugging
        if (isValidLocation) {
            binding.position.text = getString(R.string.andadi_dalam_kelas)
        } else {
            binding.position.text = getString(R.string.andadi_luar_kelas)
        }
    }

}
