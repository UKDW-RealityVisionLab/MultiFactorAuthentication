package com.mfa.view

import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.mfa.R
import com.mfa.databinding.ActivityHomeBinding
import com.mfa.utils.PreferenceUtils
import java.text.SimpleDateFormat
import java.util.Date


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val TAG = "HomeActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.namaUser.text = PreferenceUtils.getUsername(this)

        binding.btnAddFace.setOnClickListener {
            val intent = Intent(this, RegisterFaceActivity::class.java)
            startActivity(intent)
        }
        setDate()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        val faceEmbedings = PreferenceUtils.getFaceEmbeddings(this)

        //check if user embedding already exist
        if (faceEmbedings.isNotEmpty()) {
            binding.btnAddFace.visibility = View.GONE
        } else {
            binding.btnAddFace.visibility = View.VISIBLE
        }
//        if (faceEmbedings.size > 0){
//            AlertDialog.Builder(this).apply {
//                setTitle("PERHATIAN")
//                setMessage(getString(R.string.alertAddregisFace))
//                setPositiveButton("OKE") { _, _ ->
//                    startActivity(Intent(this@HomeActivity,RegisterFaceActivity::class.java))
//                }
//                create()
//                show()
//            }
//        }
//        else{
////            not show alert
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.buttonLogout ->{
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task: Task<Void?>? ->
                        PreferenceUtils.clearData(applicationContext)
                        val intent =
                            Intent(this, SplashScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(this, "Gagal keluar Applikasi :" + e.message, Toast.LENGTH_LONG).show()
                    }
                return true
            }
            else ->  return super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDate(){
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = Date()
        val current = formatter.format(date)

        binding.tglCard.text=current.toString()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Keluar")
            setMessage("Anda yakin ingin keluar dari aplikasi?")
            setPositiveButton("Ya") { _, _ ->
                finishAffinity()
            }
            setNegativeButton("Tidak", null)
            create()
            show()
        }
    }
}