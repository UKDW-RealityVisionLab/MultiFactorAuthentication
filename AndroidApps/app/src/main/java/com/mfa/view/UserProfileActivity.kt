package com.mfa.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mfa.databinding.ActivityUserProfileBinding
import com.mfa.utils.PreferenceUtils
import com.mfa.utils.Utils

class UserProfileActivity : AppCompatActivity() {
    private val TAG = "UserProfileActivity"
    private lateinit var binding: ActivityUserProfileBinding

    private val embedding: ArrayList<String> = ArrayList()
    private val openFaceVerificationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val embedding = it.data?.getStringArrayListExtra(FaceProcessorActivity.EXTRA_FACE_EMBEDDING)
                embedding?.let { data ->
                    this.embedding.clear()
                    this.embedding.addAll(data)
                }
                Log.d(TAG, "embedding : " + embedding.toString())
                binding.txtDataWajah.text = "Data berubah"
            } else {
                Log.i(TAG, "Result not OK: " + it.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val faceEmbedings = PreferenceUtils.getFaceEmbeddings(this)

        //check if user embedding already exist
        if (faceEmbedings.size > 0) {
            binding.txtDataWajah.text = "Data wajah tersedia"
        } else {
            binding.txtDataWajah.text = "Data kosong"
        }
        binding.btnFaceDetection.setOnClickListener {
            val intent = Intent(this, FaceProcessorActivity::class.java)
            openFaceVerificationLauncher.launch(intent)
        }

        binding.btnSimpanProfile.setOnClickListener {
            Utils.setFirebaseEmbedding(this.embedding)
            PreferenceUtils.saveFaceEmbeddings(applicationContext, this.embedding)
        }
    }
}