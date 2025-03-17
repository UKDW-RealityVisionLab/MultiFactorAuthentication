package com.mfa.view.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.mfa.R
import com.mfa.databinding.ActivitySimpanwajahBinding
import com.mfa.camerax.CameraManager
import com.mfa.databinding.DialogAddFaceBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpanwajahBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.title="Simpan wajah"

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

        // Inisialisasi Face Recognizer
        faceRecognizer = FaceRecognizer(assets)

        // Memulai kamera saat Activity dibuka
        cameraManager.cameraStart()

        // Tombol untuk menangkap gambar wajah
        binding.buttonStopCamera.setOnClickListener {
            cameraManager.onTakeImage(this)
        }
    }

    override fun onTakeImageSuccess(image: Bitmap) {
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)

        val dialog = AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Confirm Face")
            .setPositiveButton("OK", null) // Override di onShowListener
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                dialog.dismiss() // Tutup dialog konfirmasi dulu

                // Tampilkan loading setelah user menekan "OK"
                val loadingDialog = LoadingDialogFragment()
                loadingDialog.show(supportFragmentManager, "loadingDialog")

                lifecycleScope.launch(Dispatchers.Main) {
                    val embeddings = withContext(Dispatchers.IO) { faceRecognizer.getEmbeddingsOfImage(image) }

                    if (embeddings.isEmpty() || embeddings[0].isEmpty()) {
                        loadingDialog.dismiss() // Tutup loading jika gagal
                        Toast.makeText(this@Simpanwajah, "Gagal mendapatkan embeddings. Coba lagi.", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val embeddingStringList = embeddings[0].map { it.toString() }
                    Utils.setFirebaseEmbedding(embeddingStringList)
                    PreferenceUtils.saveFaceEmbeddings(applicationContext, embeddingStringList)
                    loadingDialog.dismiss()
                    showCustomDialog(
                        title = "Hasil scan wajah",
                        message = "Selamat! anda berhasil menyimpan wajah",
                        buttonText = "kembali ke home"
                    ) {
                        Toast.makeText(this@Simpanwajah, "Wajah tersimpan!", Toast.LENGTH_SHORT).show()

                        // Ambil email sebelum membuat intent baru
                        val userEmail = intent.getStringExtra("email")

                        val intent = Intent(this@Simpanwajah, HomeActivity::class.java)
                        intent.putExtra("email", userEmail)
                        startActivity(intent)
                    }
                }
            }
        }

        dialog.show()
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

    override fun onTakeImageError(exception: Exception) {
        Toast.makeText(this, "Gagal mengambil gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
    }


}
