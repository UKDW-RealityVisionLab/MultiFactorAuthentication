package com.mfa.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.math.sqrt

class FaceProcessorActivity : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private val TAG = "FaceProcessor"
    private lateinit var binding: ActivityCaptureFaceBinding
    private lateinit var cameraManager: CameraManager

    private val EMBEDDING_THRESHOLD = 0.8
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var faceRecognizer: FaceRecognizer // Face recognizer
    private lateinit var fas: FaceAntiSpoofing // Face anti-spoofing

    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        // Inisialisasi komponen
        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        cameraManager = CameraManager(
            this,
            binding.viewCameraPreview,
            binding.viewGraphicOverlay,
            this
        )

        // Cek user Firebase
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "User UID: ${user.uid}, Email: ${user.email}")
        } else {
            Log.e(TAG, "User is null")
        }

        // Inisialisasi ViewModel
        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        askCameraPermission()
        buttonClicks()

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                showCustomDialog(
//                    title = "Pemberitahuan",
//                    message = "Mohon selesaikan proses presensi",
//                    buttonText = "Oke",
//                    color = R.color.green_primary
//                ) {
//                    onResume()
//                }
                val builder = AlertDialog.Builder(this@FaceProcessorActivity,R.style.CustomAlertDialogStyle)
                builder.setTitle("Pemberitahuan")
                builder.setMessage("Apakah kamu ingin membatalkan presensi?")
                builder.setPositiveButton("Iya"){ _, _ ->
                    val back = Intent(this@FaceProcessorActivity, PresensiActivity::class.java)
                    back.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(back)
                }
                builder.setNegativeButton("Batalkan"){ _, _->
//                    system will handle it
                }
                builder.setCancelable(false)
                builder.show()
            }
        })
    }

    private fun showLoadingDialog() {
        if (loadingDialog?.isAdded == true) return // Jangan tampilkan jika sudah ada

        loadingDialog = LoadingDialogFragment()
        loadingDialog?.show(supportFragmentManager, "loadingDialog")
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun buttonClicks() {
        binding.buttonTurnCamera.setOnClickListener {
            cameraManager.changeCamera()
        }
        binding.buttonStopCamera.setOnClickListener {
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

    @SuppressLint("ResourceAsColor")
    override fun onTakeImageSuccess(image: Bitmap) {
//        binding.progressBar.visibility = View.GONE
        showLoadingDialog()
        binding.buttonStopCamera.isEnabled = true
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)

        val dialog = AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Konfirmasi wajah")
            .setPositiveButton("KONFIRMASI", null)
            .setNegativeButton("BATALKAN") { d, _ -> d.dismiss() }
            .create()


        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            dialog.window?.setBackgroundDrawableResource(R.color.gray_light) // Ganti dengan warna yang diinginkan
            // Atur padding untuk konten dialog (jika menggunakan custom view)

            // 1. Title: Putih & Bold
            dialog.findViewById<TextView>(android.R.id.title)?.apply {
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }

            // 2. Tombol Positif (Verifikasi): Teks putih + Background hijau
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(Color.WHITE)
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
            }

            // 3. Tombol Negatif (Batalkan): Teks putih + Background merah
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(Color.WHITE)
                setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            }

            // Opsional: Atur padding tombol
            listOf(AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE).forEach { buttonType ->
                dialog.getButton(buttonType)?.setPadding(32.toPx(), 16.toPx(), 32.toPx(), 16.toPx())
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                dismissLoadingDialog()
                dialog.dismiss()
            }


            okButton.setOnClickListener {
                dialog.dismiss()
//                dismissLoadingDialog()
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        //if wajah asli
                        if (antiSpoofDetection(image)!=false) {
                            val embeddings = withContext(Dispatchers.IO) { faceRecognizer.getEmbeddingsOfImage(image) }

                            if (embeddings.isEmpty() || embeddings[0].isEmpty()) {
                                Toast.makeText(this@FaceProcessorActivity, "Gagal mendapatkan embeddings. Coba lagi.", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val embeddingFloatList = embeddings[0].map { it.toString() }
                            Log.d(TAG, "Embeddings: ${embeddingFloatList.joinToString(", ")}")

                            // Proses embedding langsung di activity ini
                            handleEmbeddings(ArrayList(embeddingFloatList))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}")
                        Toast.makeText(this@FaceProcessorActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }
    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onTakeImageStart() {
//        binding.progressBar.visibility = View.VISIBLE
        // Disable tombol capture sementara
        showLoadingDialog()
//        binding.buttonStopCamera.isEnabled = false
    }
    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        // 1. Blur Detection with Laplacian
        val laplaceScore = fas.laplacian(faceBitmap)
        Log.d(TAG, "Laplacian Blur Score: $laplaceScore")
        dismissLoadingDialog()

        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Kualitas foto rendah. Pastikan wajah terlihat jelas dan tidak blur.",
                    Toast.LENGTH_LONG
                ).show()
            }
            Log.w(TAG, "Blur detected - Laplacian score too low ($laplaceScore)")
            return false
        }

        // 2. Anti-Spoofing Analysis
        Log.d(TAG, "Starting anti-spoofing analysis...")
        val startTime = System.currentTimeMillis()

        try {
            val spoofScore = fas.antiSpoofing(faceBitmap)
            val processingTime = System.currentTimeMillis() - startTime

            Log.d(TAG, """
            Anti-Spoofing Results:
            - Score: $spoofScore
            - Threshold: ${FaceAntiSpoofing.THRESHOLD}
            - Processing Time: ${processingTime}ms
        """.trimIndent())

            if (spoofScore >= FaceAntiSpoofing.THRESHOLD) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Deteksi kecurangan: Wajah tidak asli!",
                        Toast.LENGTH_LONG
                    ).show()
                    showCustomDialog("Hasil verifikasi wajah",
                        "Mohon jangan gunakan foto",
                        "Oke", R.color.red){
                    }
                }
                Log.w(TAG, "Potential spoof detected (score: $spoofScore)")
            }
            // if wajah dianggap asli == true
            return spoofScore < FaceAntiSpoofing.THRESHOLD
        } catch (e: Exception) {
            Log.e(TAG, "Anti-spoofing failed: ${e.message}", e)
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Gagal memproses deteksi wajah. Coba lagi.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return false
        }
    }

    override fun onTakeImageError(exception: Exception) {
//        binding.progressBar.visibility = View.GONE
        dismissLoadingDialog()
        binding.buttonStopCamera.isEnabled = true
        Toast.makeText(this, "Gagal mengambil foto: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    private fun handleEmbeddings(embeddingList: ArrayList<String>) {
        // Tampilkan loading saat proses verifikasi dimulai
         showLoadingDialog()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d(TAG, "handleEmbeddings called")
                val newEmbedding = embeddingList.map { it.toFloat() }.toFloatArray()
                Log.d(TAG, "New embedding: ${newEmbedding.toList()}")

                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.e(TAG, "User is null")
                    Toast.makeText(this@FaceProcessorActivity, "User tidak ditemukan!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Ambil data embedding dari Firebase
                val savedEmbeddingList = withContext(Dispatchers.IO) {
                    Utils.getFirebaseEmbedding(user).get().await()
                        .let { dataSnapshot ->
                            (dataSnapshot.value as? List<*>)?.map { it.toString().toFloat() }?.toFloatArray()
                        }
                }

                Log.d(TAG, "Saved embedding: ${savedEmbeddingList?.toList()}")

                if (savedEmbeddingList != null) {
                    val similarity = calculateCosineSimilarity(newEmbedding, savedEmbeddingList)
                    if (similarity > EMBEDDING_THRESHOLD) {
                        Toast.makeText(this@FaceProcessorActivity, "Face verification success!", Toast.LENGTH_LONG).show()
                        showCustomDialog(
                            title = "Hasil verifikasi wajah",
                            message = "Selamat! Anda berhasil menyelesaikan verifikasi wajah.",
                            buttonText = "Lihat status presensi",
                            color = R.color.green_primary
                        ) {
                            dismissLoadingDialog()
                            reqFaceApi()
                        }
                    } else {
                        Toast.makeText(this@FaceProcessorActivity, "Face verification failed!", Toast.LENGTH_LONG).show()
                        showCustomDialog(
                            title = "Hasil verifikasi wajah",
                            message = "Maaf, kami gagal mengenali Anda. Mohon gunakan wajah anda sendiri untuk verifikasi.",
                            buttonText = "Coba lagi",
                            color = R.color.red
                        ) {
                            onResume()
                        }
                    }
                } else {
                    Toast.makeText(this@FaceProcessorActivity, "No saved embedding found!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleEmbeddings: ${e.message}")
                Toast.makeText(this@FaceProcessorActivity, "Gagal memproses verifikasi: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Sembunyikan loading setelah proses selesai
//                loadingDialog.dismiss()
                dismissLoadingDialog()
            }
        }
    }

    private fun showCustomDialog(
        title: String,
        message: String,
        buttonText: String,
        color: Int, // warna tombol
        action: () -> Unit
    ) {
        val dialog = Dialog(this).apply {
            setCancelable(false)
            setContentView(R.layout.custom_alert_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<TextView>(R.id.tvTitle).text = title
            findViewById<TextView>(R.id.tvMessage).text = message
            findViewById<Button>(R.id.btnConfirm).apply {
                text = buttonText
                setTextColor(Color.WHITE) // Warna teks
//                setBackgroundColor(color)
                val buttonColor = ContextCompat.getColor(context, color)
                backgroundTintList = ColorStateList.valueOf(buttonColor) // Warna latar
                setOnClickListener {
                    action()
                    dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun reqFaceApi() {
        val dataEmail = EmailRequest(Email.email)
        profileViewModel.getProfile(dataEmail)
        profileViewModel.getData.observe(this) {
            val nim = it.nim
            val data = UpdateStatusReq(IdJadwal.idJadwal, nim)
            profileViewModel.updateStatus(data)
            profileViewModel.getUpdateDataStatus.observe(this) { statusUpdate ->
                Log.d(TAG, "Status update: $statusUpdate")
                if (statusUpdate) {
                    StatusMhs.statusMhs = true
                    val intent = Intent(this, PresensiActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    Toast.makeText(this, "Berhasil presensi", Toast.LENGTH_SHORT).show()
                } else {
                    StatusMhs.statusMhs = false
                    Toast.makeText(this, "Gagal verifikasi wajah", Toast.LENGTH_SHORT).show()
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
        const val EXTRA_FACE_EMBEDDING = "EXTRA_FACE_EMBEDDING"
    }
}