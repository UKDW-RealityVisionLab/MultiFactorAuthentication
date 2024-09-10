package com.mfa.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.mfa.R
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.StatusReq
import com.mfa.databinding.ActivityFaceVerificationBinding
import com.mfa.di.Injection
import com.mfa.utils.Utils
import com.mfa.view.Email
import com.mfa.view.IdJadwal
import com.mfa.view.IdJadwal.idJadwal
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.math.sqrt

class FaceVerificationActivity : AppCompatActivity() {
    private val TAG = "FaceVerificationActivity"
    private lateinit var binding: ActivityFaceVerificationBinding
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private val EMBEDDING_THRESHOLD = 0.8
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val embeddings = result.data?.getStringArrayListExtra(FaceProcessorActivity.EXTRA_FACE_EMBEDDING)
                embeddings?.let { data ->
                    handleEmbeddings(data)
                }
            } else {
                Log.i(TAG, "Result not OK: ${result.toString()}")
            }
        }
        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnFaceDetection.setOnClickListener {
            val intent = Intent(this, FaceProcessorActivity::class.java)
            takePhotoLauncher.launch(intent)
        }
    }

    private fun handleEmbeddings(embeddingList: ArrayList<String>) {
        val newEmbedding = embeddingList.map { it.toFloat() }.toFloatArray()
        Log.d(TAG, "New embedding: ${newEmbedding.toList()}")

        Utils.getFirebaseEmbedding().get().addOnSuccessListener { dataSnapshot ->
            val savedEmbeddingList = (dataSnapshot.value as? List<*>)?.map { it.toString().toFloat() }?.toFloatArray()

            if (savedEmbeddingList != null) {
                val similarity = calculateCosineSimilarity(newEmbedding, savedEmbeddingList)
                if (similarity > EMBEDDING_THRESHOLD) {
                    showVerificationSuccessDialog()
                } else {
                    Toast.makeText(this, "Face verification failed!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No saved embedding found!", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching saved embeddings: ${exception.message}")
        }
    }


    private fun showVerificationSuccessDialog() {
        reqFaceApi()
        AlertDialog.Builder(this)
            .setTitle("Verification Successful")
            .setMessage("Face has been verified successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun reqFaceApi(){
        profileViewModel.getData.observe(this){
            val nim= it.nim
            val data= StatusReq(IdJadwal.idJadwal, nim)
            profileViewModel.updateStatus(data)
            profileViewModel.getDataStatus.observe(this){ statusUpdate->
                Log.d("status update"," $data $statusUpdate ${it.email}" )
                if (statusUpdate == true){
                   startActivity(Intent(this, PresensiActivity::class.java))
                }else if (statusUpdate == false){
                    Toast.makeText(this,"failed face verify",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun calculateCosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        val dotProduct = vecA.zip(vecB).sumOf { (a, b) -> (a * b).toDouble() }.toFloat()
        val magnitudeA = sqrt(vecA.sumOf { (it * it).toDouble() }).toFloat()
        val magnitudeB = sqrt(vecB.sumOf { (it * it).toDouble() }).toFloat()
        return dotProduct / (magnitudeA * magnitudeB)
    }
}
