package com.mfa.view.activity

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
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
import com.mfa.camerax.CameraEkspresi
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityCaptureFaceBinding
import com.mfa.databinding.DialogAddFaceBinding
import com.mfa.di.Injection
import com.mfa.facedetector.EkspresiRecognizer
import com.mfa.facedetector.FaceAntiSpoofing
import com.mfa.facedetector.FaceRecognizer
import com.mfa.`object`.Email
import com.mfa.`object`.IdJadwal
import com.mfa.`object`.StatusMhs
import com.mfa.preprocessor.PreprocessingUtils
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
import kotlin.math.pow
import kotlin.math.sqrt

class FaceProcessorActivity : AppCompatActivity() {
    private val TAG = "FaceProcessor"
    private lateinit var binding: ActivityCaptureFaceBinding
    private lateinit var cameraEkspresi: CameraEkspresi
    private lateinit var ekspresiRecognizer: EkspresiRecognizer
    private val EMBEDDING_THRESHOLD = 0.8f
    private lateinit var profileViewModel: ProfileViewModel


    private val allExpressions = listOf("senyum", "hadap kiri", "hadap kanan", "kedip", "tutup mata kiri", "tutup mata kanan")

    //mengambil 5 ekspresi random
    private val selectedExpressions = allExpressions.shuffled().take(5).toMutableList()
    private var currentIndex = 0
    private fun startExpressionChallenge() {
        if (currentIndex < selectedExpressions.size) {
            val currentExpression = selectedExpressions[currentIndex]
            Log.d("FaceProcessor", "Mulai tantangan ekspresi: $currentExpression") // 🔥 Log ekspresi
            binding.expressionCommandText.text = "Yuk coba berekspresi: $currentExpression"
        } else {
            Log.d("FaceProcessor", "Semua ekspresi selesai! Mulai verifikasi wajah.")
            startFaceVerification()
        }
    }




    private var start_verify = false
    private var exp_face_ok = false
    private var verify_counter = 0
    lateinit var face_image:Bitmap

    private lateinit var faceRecognizer: FaceRecognizer // Face recognizer
    private lateinit var fas: FaceAntiSpoofing // Face anti-spoofing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ekspresiRecognizer = EkspresiRecognizer { expression -> handleDetectedExpression(expression) }
        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        cameraEkspresi = CameraEkspresi(this, binding.previewView, this) { expression -> handleDetectedExpression(expression) }

        cameraEkspresi.cameraStart()

        profileViewModel = ViewModelProvider(
            this,
            ViewModelFactory(Injection.provideRepository(this))
        ).get(ProfileViewModel::class.java)

        askCameraPermission()

        // **Pastikan ekspresi pertama muncul**
        startExpressionChallenge()  // 🔥 Tambahkan ini agar perintah pertama muncul

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCustomDialog(
                    title = "Pemberitahuan",
                    message = "Mohon selesaikan proses presensi",
                    buttonText = "Oke",
                    color = R.color.green_primary
                ) { onResume() }
            }
        })
    }


    private fun askCameraPermission() {
        if (arrayOf(android.Manifest.permission.CAMERA).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            cameraEkspresi.cameraStart()
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
            cameraEkspresi.cameraStart()
        } else {
            Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }


    val handler = Handler(Looper.getMainLooper())
    fun stop_handler(){
        //handler.removeCallbacks(runnableDetectionHandler)
        on_detect = false;
        if(!exp_face_ok){
            verifyFace(face_image)
            exp_face_ok = true
        }
    }
    private var on_detect = false
    val runnableDetectionHandler = object : Runnable {
        override fun run() {
            if(on_detect) {
                cameraEkspresi.onTakeImage(object : CameraEkspresi.OnTakeImageCallback {
                    override fun onTakeImageSuccess(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            return
                        }
                        runOnUiThread {
                            /*if (verify_counter < 5) {
                                binding.expressionCommandText.text =
                                    "Verifikasi wajah " + (verify_counter + 1)
                                binding.imageViewPreview.setImageBitmap(bitmap)
                                verify_counter++;
                            } else {*/
                            var width = bitmap.width
                            var height = bitmap.height
                            var x = width /10
                            width = width - (x*2)
                            val resizedBmp: Bitmap = Bitmap.createBitmap(bitmap, x, 0, width, height)
                            binding.imageViewPreview.setImageBitmap(resizedBmp)
                            face_image = resizedBmp
                            stop_handler()
                            //}
                        }
                    }

                    override fun onTakeImageError(exception: ImageCaptureException) {
                    }
                })
                if (verify_counter < 5) {
                    handler.postDelayed(this, 500)
                } else {
                    handler.removeCallbacks(this)
                }
            }
        }
    }

    //usage because photo cannot make an expression
    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        val laplaceScore: Int = fas.laplacian(faceBitmap)
        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            Toast.makeText(this, "Image too blurry!", Toast.LENGTH_LONG).show()
            return false
        }

        val start = System.currentTimeMillis()
        val score = fas.antiSpoofing(faceBitmap)
        val end = System.currentTimeMillis()
        Log.d(TAG, "Spoof detection process time: ${end - start} ms")

        return score < FaceAntiSpoofing.THRESHOLD
    }

    private var isreset = false
    private var time_millis = System.currentTimeMillis();
    private var isCapturing = false
    private var verify_face = false

    fun handleDetectedExpression(expression: String) {
        if (currentIndex >= selectedExpressions.size) {
            Log.e("FaceVerification", "Index melebihi batas!")
            return
        }

        if (isreset || isCapturing || verify_face) {
            Log.d("FaceVerification", "Proses diabaikan karena state: reset=$isreset, capturing=$isCapturing, verify=$verify_face")
            return
        }

        if (expression.equals(selectedExpressions[currentIndex], ignoreCase = true)) {
            Log.d("FaceVerification", "✅ Ekspresi cocok: $expression, mengambil gambar...")

            if (currentIndex < selectedExpressions.size - 1) {
                // 🔹 Tahap 1-4: Tidak ada auto capture, hanya lanjut ke ekspresi berikutnya
                currentIndex++
                runOnUiThread {
                    binding.expressionCommandText.text = "Sekarang tunjukkan eksprei: ${selectedExpressions[currentIndex]}"
                }
            } else {
                // 🔹 Tahap 5: Auto capture 1 kali saja, lalu tampilkan dialog
                Log.d("FaceVerification", "Tahap 5! Auto capture 1 kali...")
                binding.expressionCommandText.text= "Pemanasan selesai"
                isCapturing = true // Mencegah auto capture berulang

                cameraEkspresi.onTakeImage(object : CameraEkspresi.OnTakeImageCallback {
                    override fun onTakeImageSuccess(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            Log.e("FaceVerification", "❌ Capture gagal!")
                            isCapturing = false
                            return
                        }

                        Log.d("FaceVerification", "📸 Auto capture tahap 5 berhasil!")

                        // **Gunakan metode cropping yang sama dengan APK lama**
                        val width = bitmap.width
                        val height = bitmap.height
                        val x = width / 10
                        val croppedBitmap: Bitmap = Bitmap.createBitmap(bitmap, x, 0, width - (x * 2), height)

                        // Simpan gambar terakhir untuk verifikasi wajah
                        face_image = croppedBitmap

                        runOnUiThread {
                            binding.imageViewPreview.visibility = View.VISIBLE
                            binding.previewView.visibility = View.VISIBLE

                            // Show the dialog only after warm-up is complete
                            showCustomDialog("Pemberitahuan",
                                "Keren, sekarang kamu siap untuk verifikasi wajahmu",
                                buttonText = "Verifikasi wajah",
                                color = R.color.green_primary){
                                Log.d("FaceVerification", "🔍 Mulai verifikasi wajah setelah tombol diklik")
                                binding.expressionCommandText.text="Mohon tahan posisi hp dan wajah anda \n dalam beberapa detik..."
                                binding.imageViewPreview.visibility = View.GONE
                                binding.previewView.visibility = View.VISIBLE
                                binding.verifyButton.visibility = View.INVISIBLE

                                isCapturing = false
                                verify_face = false
                                start_verify = true
                                time_millis = System.currentTimeMillis()

                                startFaceVerification()
                            }
                        }
                    }

                    override fun onTakeImageError(exception: ImageCaptureException) {
                        Log.e("FaceVerification", "❌ Capture gagal: ${exception.message}")
                        isCapturing = false
                    }
                })
            }
        } else {
            Log.d("FaceVerification", "❌ Ekspresi tidak cocok: $expression, menunggu ekspresi: ${selectedExpressions[currentIndex]}")
        }
    }



    private fun flipBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun verifyFace(bitmap: Bitmap) {
        Log.d("FaceVerification", "Memulai verifikasi wajah...")

        // Pastikan bitmap tidak null
        if (bitmap == null) {
            Log.e("FaceVerification", "Bitmap yang diberikan null!")
            return
        }

        var processedBitmap = bitmap
        Log.d("FaceVerification", "Memproses gambar untuk ekstraksi embedding: ${processedBitmap.width}x${processedBitmap.height}")
        // Flip jika menggunakan kamera depan
        if (CameraManager.cameraOption == CameraSelector.LENS_FACING_FRONT) {
            processedBitmap = flipBitmap(processedBitmap)
            Log.d("FaceVerification", "Gambar dibalik (flip) untuk kamera depan")
        }

        // Ubah ukuran gambar menjadi 256x256
        processedBitmap = Bitmap.createScaledBitmap(processedBitmap, 256, 256, false)
        Log.d("FaceVerification", "Ukuran Gambar setelah flip dan resize: ${processedBitmap.width}x${processedBitmap.height}")

        // Cek apakah gambar sudah siap untuk ekstraksi embedding
        if (processedBitmap.width == 0 || processedBitmap.height == 0) {
            Log.e("FaceVerification", "Ukuran gambar yang diproses tidak valid!")
            return
        }

        try {
            // Log sebelum ekstraksi embedding
            Log.d("FaceVerification", "Menyiapkan untuk ekstraksi embedding wajah... Gambar ukuran: ${bitmap.width}x${bitmap.height}")
            val embeddings = faceRecognizer.getEmbeddingsOfImage(processedBitmap)
            Log.d("FaceVerification", "Hasil ekstraksi embedding: ${embeddings?.joinToString(", ")}")

            // Cek apakah embeddings null atau kosong
            if (embeddings == null || embeddings.isEmpty()) {
                Log.e("FaceVerification", "Gagal mendapatkan embedding wajah: Hasil embeddings kosong atau null!")
            } else {
                Log.d("FaceVerification", "Embedding berhasil: ${embeddings.contentToString()}")
            }
            val embeddingList = embeddings[0]  // Ambil embedding pertama

            // Pastikan embedding tidak kosong
            if (embeddingList.isEmpty()) {
                Log.e("FaceVerification", "Embedding wajah kosong!")
                return
            }

            binding.scanLine.visibility = View.GONE
            // Lanjutkan untuk membandingkan dengan embedding yang disimpan di Firebase
            handleEmbeddings(embeddingList)
        } catch (e: Exception) {
            // Log error jika terjadi exception
            Log.e("FaceVerification", "Error saat ekstraksi embedding wajah: ${e.message}")
            e.printStackTrace()
        }
    }




    private fun startFaceVerification() {
        Log.d("FaceVerification", "Mulai verifikasi wajah (5 kali auto capture)...")
        runOnUiThread {
            binding.expressionCommandText.text = "Mohon tahan posisi hp dan wajah anda \n dalam beberapa detik..."
            binding.imageViewPreview.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            binding.verifyButton.visibility = View.GONE
            startScanLineAnimation()
        }

        // Reset counter sebelum mulai auto capture
        verify_counter = 0
        autoCaptureForVerification()
    }

    private fun startScanLineAnimation() {
        val previewView = binding.previewView
        val scanLine = binding.scanLine

        scanLine.visibility = View.VISIBLE

        // Tunggu sampai layout selesai diukur
        previewView.post {
            val targetY = previewView.height - scanLine.height

            val animator = ObjectAnimator.ofFloat(
                scanLine,
                "translationY",
                0f,
                targetY.toFloat()
            )
            animator.duration = 2000
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.repeatMode = ObjectAnimator.REVERSE
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }




    // Fungsi untuk melakukan auto capture 5 kali sebelum verifikasi wajah
    private fun autoCaptureForVerification() {
        if (verify_counter < 5) {
            cameraEkspresi.onTakeImage(object : CameraEkspresi.OnTakeImageCallback {
                override fun onTakeImageSuccess(bitmap: Bitmap?) {
                    if (bitmap == null) {
                        Log.e("FaceVerification", "Capture gagal, mencoba lagi...")
                        return
                    }

                    Log.d("FaceVerification", "Auto capture ${verify_counter + 1}/5 berhasil!")

                    // **Lakukan Cropping Seperti di Simpanwajah**
                    var width = bitmap.width
                    var height = bitmap.height
                    var x = width /10
                    width = width - (x*2)
                    val resizedBmp: Bitmap = Bitmap.createBitmap(bitmap, x, 0, width, height)

                    val p = PreprocessingUtils()
                    var greyPixels = p.convertRawGreyImg(resizedBmp);
//                    val variance = p.isBlurryD(greyPixels)
                    greyPixels = p.convolve(greyPixels, p.generateGaussianKernel(3, 3f/6f), 3);
                    val processedPixels = p.convertArrayToBitmap(greyPixels);

                    // Simpan gambar terakhir untuk verifikasi wajah
                    face_image = processedPixels
//                    binding.imageViewPreview.setImageBitmap(processedPixels)

                    verify_counter++

                    // Jika belum mencapai 5 kali, ulangi lagi auto capture dengan delay lebih cepat (0.5 detik)
                    if (verify_counter < 5) {
                        Handler(Looper.getMainLooper()).postDelayed({ autoCaptureForVerification() }, 500) // 🔥 Kurangi delay ke 500ms
                    } else {
                        Toast.makeText(this@FaceProcessorActivity, "Auto capture selesai! Mulai verifikasi wajah...", Toast.LENGTH_SHORT).show();
                        Log.d("FaceVerification", "Auto capture selesai! Mulai verifikasi wajah...")
                        verifyFace(processedPixels)  // **Pastikan yang digunakan adalah gambar yang sudah di-crop**
                    }
                }

                override fun onTakeImageError(exception: ImageCaptureException) {
                    Log.e("FaceVerification", "Capture gagal: ${exception.message}")
                    runOnUiThread {
                        showCustomDialog(
                            title = "Error",
                            message = "Gagal mengambil gambar. Silakan coba lagi.",
                            buttonText = "Coba Lagi",
                            color = R.color.red
                        ) {
                            resetVerificationProcess()
                        }
                    }
                }
            })
        }
    }





    private fun handleEmbeddings(embeddingList: FloatArray) {
        Log.d("FaceVerification", "Membandingkan embedding wajah dengan data di Firebase...")

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.e("FaceVerification", "User tidak ditemukan!")
                    Toast.makeText(this@FaceProcessorActivity, "User tidak ditemukan!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Ambil data embedding dari Firebase dengan coroutine
                val savedEmbeddingList = withContext(Dispatchers.IO) {
                    Utils.getFirebaseEmbedding(user).get().await()
                        .let { dataSnapshot ->
                            (dataSnapshot.value as? List<*>)?.mapNotNull { it.toString().toFloatOrNull() }?.toFloatArray()
                        }
                }

                if (savedEmbeddingList == null || savedEmbeddingList.isEmpty()) {
                    Log.e("FaceVerification", "Data embedding dari Firebase kosong atau tidak valid!")
                    return@launch
                }

                Log.d("FaceVerification", "Ukuran embedding di Firebase: ${savedEmbeddingList.size}")
                Log.d("FaceVerification", "Ukuran embedding wajah yang diverifikasi: ${embeddingList.size}")

                if (savedEmbeddingList.size != embeddingList.size) {
                    Log.e("FaceVerification", "Ukuran embedding tidak cocok! Firebase: ${savedEmbeddingList.size}, Verifikasi: ${embeddingList.size}")
                    return@launch
                }

                val similarity = cosineDistance(embeddingList, savedEmbeddingList)
                Log.d("FaceVerification", "Hasil Similarity: $similarity")



                if (similarity > EMBEDDING_THRESHOLD) {
                    Log.d("FaceVerification", "Wajah terverifikasi! Similarity: $similarity")
                    binding.expressionCommandText.text = "verifikasi wajah berhasil"
                    Toast.makeText(this@FaceProcessorActivity, "Verifikasi wajah berhasil", Toast.LENGTH_LONG).show()
                    showCustomDialog(
                        title = "Hasil verifikasi wajah",
                        message = "Selamat anda telah berhasil untuk menyelesaikan semua persyaratan  yang digunakan untuk presensi",
                        buttonText = "Lihat status presensi",
                        color = R.color.green_primary){
                        reqFaceApi()
                    }
                } else {
                    Log.e("FaceVerification", "Verifikasi wajah gagal! Similarity: $similarity")
                    binding.expressionCommandText.text = "Verifikasi wajah gagal!"
                    Handler(Looper.getMainLooper()).postDelayed({
                        showCustomDialog(
                            title = "Hasil verifikasi wajah",
                            message = "Maaf kami gagal mengenali anda. Mohon menggunakan wajah anda sendiri untuk verifikasi wajah",
                            buttonText = "Coba lagi",
                            color = R.color.red
                        ) {
                            // Panggil fungsi reset ketika tombol "Coba lagi" diklik
                            resetVerificationProcess()
                        }
                    }, 1000)
                }
            } catch (e: Exception) {
                Log.e("FaceVerification", "Gagal mengambil data embedding dari Firebase: ${e.message}")
                Toast.makeText(this@FaceProcessorActivity, "Gagal memproses verifikasi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resetVerificationProcess() {
        Log.d("FaceVerification", "Memulai ulang proses verifikasi...")

        // Reset semua state
        currentIndex = 0
        isCapturing = false
        verify_face = false
        start_verify = false
        exp_face_ok = false
        verify_counter = 0

        // Acak ulang ekspresi
        selectedExpressions.clear()
        selectedExpressions.addAll(allExpressions.shuffled().take(5))
        Log.d("FaceVerification", "Ekspresi baru: $selectedExpressions")

        // Reset UI
        runOnUiThread {
            binding.imageViewPreview.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            binding.verifyButton.visibility = View.GONE
            binding.expressionCommandText.text = "Memulai verifikasi wajah..."
        }

        // Restart kamera dengan delay untuk memastikan
        lifecycleScope.launch {
            cameraEkspresi.cameraStop()
            delay(300) // Beri waktu untuk kamera benar-benar berhenti

            // Inisialisasi ulang recognizer
            ekspresiRecognizer = EkspresiRecognizer { expression ->
                handleDetectedExpression(expression)
            }

            cameraEkspresi = CameraEkspresi(
                this@FaceProcessorActivity,
                binding.previewView,
                this@FaceProcessorActivity
            ) { expression ->
                handleDetectedExpression(expression)
            }

            cameraEkspresi.cameraStart()
            delay(300) // Beri waktu kamera untuk inisialisasi

            // Mulai tantangan ekspresi
            runOnUiThread {
                startExpressionChallenge()
                Toast.makeText(
                    this@FaceProcessorActivity,
                    "Silakan lakukan ekspresi yang diminta",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun cosineDistance(x1: FloatArray, x2: FloatArray): Float {
        val x1norm = normalizeVector(x1)
        val x2norm = normalizeVector(x2)
        var mag1 = 0.0f
        var mag2 = 0.0f
        var product = 0.0f
        for (i in x1norm.indices) {
            mag1 += x1norm[i].pow(2)
            mag2 += x2norm[i].pow(2)
            product += x1norm[i] * x2norm[i]
        }
        mag1 = sqrt(mag1)
        mag2 = sqrt(mag2)
        var recog = (product / (mag1 * mag2)) * 1.05f
        if (recog > 1.0f) {
            recog = 1.0f
        }
        return recog
    }

    private fun normalizeVector(vector: FloatArray): FloatArray {
        val magnitude = sqrt(vector.map { it * it }.sum().toDouble()).toFloat()
        return if (magnitude != 0f) vector.map { it / magnitude }.toFloatArray() else vector
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FaceVerification", "Menutup kamera...")
        cameraEkspresi.cameraStop()
    }

    override fun onStart() {
        super.onStart()
        binding.expressionCommandText.visibility=View.INVISIBLE
        showCustomDialog("Pemberitahuan","Mohon ikuti perintah yang diberikan","Oke", color = R.color.green_primary){
            showCustomDialog("Pemberitahuan","Sebelum verifikasi, pemanasan dulu yuk!","Mulai", color = R.color.green_primary){
                resetVerificationProcess()
                binding.expressionCommandText.visibility=View.VISIBLE
            }
        }

    }

    companion object {
        const val EXTRA_FACE_EMBEDDING = "EXTRA_FACE_EMBEDDING"
    }
}