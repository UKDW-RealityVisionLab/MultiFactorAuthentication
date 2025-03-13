package com.mfa.view.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mfa.R
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.UpdateStatusReq
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityCaptureFaceBinding
import com.mfa.databinding.DialogAddFaceBinding
import com.mfa.di.Injection
import com.mfa.facedetector.FaceAntiSpoofing
import com.mfa.facedetector.FaceRecognizer
import com.mfa.`object`.Email
import com.mfa.`object`.IdJadwal
import com.mfa.`object`.StatusMhs
import com.mfa.utils.Utils
import com.mfa.view.custom.LoadingDialogFragment
import com.mfa.view_model.ProfileViewModel
import com.mfa.view_model.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FaceProcessorActivity : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private val TAG = "Face prosesor"
    private lateinit var binding: ActivityCaptureFaceBinding
    private lateinit var cameraManager: CameraManager


    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private val EMBEDDING_THRESHOLD = 0.8
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var faceRecognizer: FaceRecognizer // face recognizer
    private lateinit var fas: FaceAntiSpoofing // face antispoof
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        cameraManager = CameraManager(
            this,
            binding.viewCameraPreview,
            binding.viewGraphicOverlay,
            this
        )


        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        askCameraPermission()
        buttonClicks()

        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val embeddings = result.data?.getStringArrayListExtra(FaceProcessorActivity.EXTRA_FACE_EMBEDDING)
                embeddings?.let { data ->
                    Log.d(TAG, "Data yang diterima dari intent: $embeddings")

                    handleEmbeddings(data)
                }
            } else {
                Log.i(TAG, "Result not OK: ${result.toString()}")
            }
        }
    }

    private fun buttonClicks() {
        binding.buttonTurnCamera.setOnClickListener {
            cameraManager.changeCamera()
        }
        binding.buttonStopCamera.setOnClickListener {
            //todo : show loading screen when processing
//            binding.progressBar.visibility= View.VISIBLE
            cameraManager.onTakeImage(this)

        }
    }

    private fun askCameraPermission() {
        if (arrayOf(android.Manifest.permission.CAMERA).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            cameraManager.cameraStart()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraManager.cameraStart()
        } else {
            Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTakeImageSuccess(image: Bitmap) {
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)

        val dialog = AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Confirm Face")
            .setPositiveButton("OK", null) // Null supaya bisa kita override di onShowListener
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                dialog.dismiss() // Tutup dialog konfirmasi dulu

                // Munculkan loading setelah user menekan "OK"
                val loadingDialog = LoadingDialogFragment()
                loadingDialog.show(supportFragmentManager, "loadingDialog")

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        if (antiSpoofDetection(image)) {
                            val embeddings = withContext(Dispatchers.IO) { faceRecognizer.getEmbeddingsOfImage(image) }

                            if (embeddings.isEmpty() || embeddings[0].isEmpty()) {
                                Toast.makeText(this@FaceProcessorActivity, "Gagal mendapatkan embeddings. Coba lagi.", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val embeddingFloatList = embeddings[0].map { it.toString() }
                            val intent = Intent().apply {
                                putStringArrayListExtra(EXTRA_FACE_EMBEDDING, ArrayList(embeddingFloatList))
                            }
                            setResult(RESULT_OK, intent)

                            delay(3000) // Tampilkan loading sebentar sebelum hasilnya keluar

                            showCustomDialog(
                                title = "Hasil verifikasi wajah",
                                message = "Selamat! Anda berhasil menyelesaikan verifikasi wajah.",
                                buttonText = "Lihat status presensi"
                            ) {
                                reqFaceApi()
                            }
                        } else {
                            setResult(RESULT_CANCELED)
                            showCustomDialog(
                                title = "Hasil verifikasi wajah",
                                message = "Maaf, kami gagal mengenali Anda. Mohon gunakan wajah Anda sendiri untuk verifikasi.",
                                buttonText = "Coba lagi"
                            ) {
                                onResume()
                            }
                        }
                    } finally {
                        loadingDialog.dismiss() // Tutup loading setelah semua proses selesai
                    }
                }
            }
        }

        dialog.show()
    }


    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        //preprocessing part
        val laplaceScore: Int = fas.laplacian(faceBitmap)
        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            Toast.makeText(this, "Image too blurry!", Toast.LENGTH_LONG).show()
        } else {
            // Liveness detection
            val start = System.currentTimeMillis()
            val score = fas.antiSpoofing(faceBitmap)
            val end = System.currentTimeMillis()
            Log.d(TAG, "Spoof detection process time : " + (end - start))
            if (score < FaceAntiSpoofing.THRESHOLD) {
                return true
            }
            Toast.makeText(this, "Face are spoof!", Toast.LENGTH_LONG).show()
        }
        return false
    }


    override fun onTakeImageError(exception: ImageCaptureException) {
        Toast.makeText(this, "onTakeImageError : " + exception.message, Toast.LENGTH_SHORT).show()
    }

    private fun handleEmbeddings(embeddingList: ArrayList<String>) {
        val newEmbedding = embeddingList.map { it.toFloat() }.toFloatArray()
        Log.d(TAG, "New embedding: ${newEmbedding.toList()}")

        Utils.getFirebaseEmbedding().get().addOnSuccessListener { dataSnapshot ->
            val savedEmbeddingList = (dataSnapshot.value as? List<*>)?.map { it.toString().toFloat() }?.toFloatArray()

            if (savedEmbeddingList != null) {
                val similarity = calculateCosineSimilarity(newEmbedding, savedEmbeddingList)
                if (similarity > EMBEDDING_THRESHOLD) {
//                    showVerificationSuccessDialog()
//                    reqFaceApi()
                    Toast.makeText(this, "Face verification success!", Toast.LENGTH_LONG).show()
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


    private fun showCustomDialog(title: String, message: String, buttonText: String, action: () -> Unit) {
        val dialog = Dialog(this)
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null)

        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Hapus background default

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        tvTitle.text = title
        tvMessage.text = message
        btnConfirm.text = buttonText

        btnConfirm.setOnClickListener {
            action() // Eksekusi aksi yang dikirim dari parameter
            dialog.dismiss() // Tutup dialog setelah aksi
        }

        dialog.show()
    }


//    private fun showVerificationSuccessDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("Verification Successful")
//            .setMessage("Face has been verified successfully!")
//            .setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//                val intent = Intent(this, PresensiActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                startActivity(intent)
//                finish()
//            }
//            .show()
//    }

    private fun reqFaceApi(){
        val dataEmail = EmailRequest(Email.email)
        profileViewModel.getProfile(dataEmail)
        profileViewModel.getData.observe(this){
            val nim= it.nim
            val data= UpdateStatusReq(IdJadwal.idJadwal, nim)
            profileViewModel.updateStatus(data)
            profileViewModel.getUpdateDataStatus.observe(this){ statusUpdate->
                Log.d("status update"," $data $statusUpdate ${it.nim}" )
                if (statusUpdate){
                    StatusMhs.statusMhs= true
                    Log.d("status update to be","${StatusMhs.statusMhs}")
                    val intent = Intent(this, PresensiActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    Toast.makeText(this,"Berhasil presensi",Toast.LENGTH_SHORT).show()
                }else if (statusUpdate == false){
                    StatusMhs.statusMhs= false
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

    companion object {
        val EXTRA_FACE_EMBEDDING = "EXTRA_FACE_EMBEDDING"
    }
}