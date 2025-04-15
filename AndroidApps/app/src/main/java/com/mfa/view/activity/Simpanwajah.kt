package com.mfa.view.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mfa.R
import com.mfa.databinding.ActivitySimpanwajahBinding
import com.mfa.camerax.CameraManager
import com.mfa.databinding.DialogAddFaceBinding
import com.mfa.facedetector.FaceAntiSpoofing
import com.mfa.facedetector.FaceRecognizer
import com.mfa.utils.PreferenceUtils
import com.mfa.utils.Utils
import com.mfa.view.custom.LoadingDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class Simpanwajah : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private lateinit var binding: ActivitySimpanwajahBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var faceRecognizer: FaceRecognizer
    private lateinit var fas: FaceAntiSpoofing

    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpanwajahBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.title="Simpan wajah"

        // Inisialisasi Face Recognizer
        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        // Inisialisasi CameraManager untuk menangkap gambar
        cameraManager = CameraManager(
            this,
            binding.viewCameraPreview,
            binding.viewGraphicOverlay,
            this
        )

        binding.buttonTurnCamera.setOnClickListener {
            cameraManager.changeCamera()
        }

//        faceRecognizer = FaceRecognizer(assets)

        // Memulai kamera saat Activity dibuka
        cameraManager.cameraStart()

        // Tombol untuk menangkap gambar wajah
        binding.buttonStopCamera.setOnClickListener {
            cameraManager.onTakeImage(this)
        }
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

    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        // 1. Blur Detection with Laplacian
        val laplaceScore = fas.laplacian(faceBitmap)
        Log.d("simpan wajah", "Laplacian Blur Score: $laplaceScore")
        dismissLoadingDialog()

        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
                Toast.makeText(
                    this,
                    "Kualitas foto rendah. Pastikan wajah terlihat jelas dan tidak blur.",
                    Toast.LENGTH_LONG
                ).show()

            Log.w("simpan wajah", "Blur detected - Laplacian score too low ($laplaceScore)")
            return false
        }

        // 2. Anti-Spoofing Analysis
        Log.d("simpan wajah", "Starting anti-spoofing analysis...")
        val startTime = System.currentTimeMillis()

        try {
            val spoofScore = fas.antiSpoofing(faceBitmap)
            val processingTime = System.currentTimeMillis() - startTime

            Log.d("simpan wajah", """
            Anti-Spoofing Results:
            - Score: $spoofScore
            - Threshold: ${FaceAntiSpoofing.THRESHOLD}
            - Processing Time: ${processingTime}ms
        """.trimIndent())

            if (spoofScore >= FaceAntiSpoofing.THRESHOLD) {
                    Toast.makeText(
                        this,
                        "Deteksi kecurangan: Wajah tidak asli!",
                        Toast.LENGTH_LONG
                    ).show()
                    showCustomDialog("Hasil verifikasi wajah",
                        "Mohon jangan gunakan foto",
                        "Oke", R.color.red){

                    }

                Log.w("simpan wajah", "Potential spoof detected (score: $spoofScore)")
            }

            return spoofScore < FaceAntiSpoofing.THRESHOLD
        } catch (e: Exception) {
            Log.e("simpan wajah", "Anti-spoofing failed: ${e.message}", e)
                Toast.makeText(
                    this,
                    "Gagal memproses deteksi wajah. Coba lagi.",
                    Toast.LENGTH_LONG
                ).show()

            return false
        }
    }
    @SuppressLint("ResourceAsColor")
    override fun onTakeImageSuccess(image: Bitmap) {
//        binding.progressBar.visibility = View.GONE
        binding.buttonStopCamera.isEnabled = true
//        val loadingDialog = LoadingDialogFragment()
        dismissLoadingDialog()
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)

        val dialog = AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Konfirmasi wajah")
            .setPositiveButton("KONFIRMASI", null)
            .setNegativeButton("BATALKAN") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
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

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                showLoadingDialog()
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
//                        Log.d("simpan wajah antispoof","${antiSpoofDetection(image)}")
                        //if wajah palsu brarti false
                        if(antiSpoofDetection(image) === false){
                            dialog.dismiss()
                            return@launch
                        }else{
                            val embeddings = withContext(Dispatchers.IO) { faceRecognizer.getEmbeddingsOfImage(image) }

                            if (embeddings.isEmpty() || embeddings[0].isEmpty()) {
//                                loadingDialog.dismiss()
//                                dismissLoadingDialog()
                                Toast.makeText(this@Simpanwajah, "Gagal mendapatkan embeddings. Coba lagi.", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val embeddingStringList = embeddings[0].map { it.toString() }

                            // Pastikan user sudah login sebelum menyimpan
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                Utils.setFirebaseEmbedding(embeddingStringList)
                                PreferenceUtils.saveFaceEmbeddings(applicationContext, embeddingStringList)

//                                loadingDialog.dismiss()
//                                dismissLoadingDialog()
                                dialog.dismiss()
                                showCustomDialog(
                                    title = "Hasil scan wajah",
                                    message = "Selamat! anda berhasil menyimpan wajah",
                                    buttonText = "kembali ke home", color = R.color.green_primary
                                ) {
                                    Toast.makeText(this@Simpanwajah, "Wajah tersimpan!", Toast.LENGTH_SHORT).show()
                                    val userEmail = intent.getStringExtra("email")
                                    val intent = Intent(this@Simpanwajah, HomeActivity::class.java)
                                    intent.putExtra("email", userEmail)
                                    startActivity(intent)
                                }
                            } else {
                                Toast.makeText(this@Simpanwajah, "Anda harus login terlebih dahulu", Toast.LENGTH_LONG).show()
                            }
                        }

                    } catch (e: Exception) {
//                        loadingDialog.dismiss()
//                        dismissLoadingDialog()
                        Toast.makeText(this@Simpanwajah, "Terjadi kesalahan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        Log.e("SaveFace", "Error saving face", e)
                    }finally {
                        dismissLoadingDialog()
                    }
                }
            }
        }

        dialog.show()
    }

    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


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

    override fun onTakeImageError(exception: Exception) {
//        binding.progressBar.visibility = View.GONE
//        val loadingDialog = LoadingDialogFragment()
//        loadingDialog.dismiss()
        dismissLoadingDialog()
        binding.buttonStopCamera.isEnabled = true
        Toast.makeText(this, "Gagal mengambil gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onTakeImageStart() {
//        binding.progressBar.visibility = View.VISIBLE
        // Disable tombol capture sementara
        binding.buttonStopCamera.isEnabled = false
        showLoadingDialog()
//        val loadingDialog = LoadingDialogFragment()
//        loadingDialog.show(supportFragmentManager, "loadingDialog")
    }


}
