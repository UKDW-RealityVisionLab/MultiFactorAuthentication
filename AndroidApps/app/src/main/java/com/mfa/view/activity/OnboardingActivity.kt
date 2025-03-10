package com.mfa.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mfa.R
import com.mfa.`object`.Email
import com.mfa.utils.PreferenceUtils
import com.mfa.utils.Utils
import com.mfa.view.adapter.OnboardingPagerAdapter
import com.mfa.view.fragment.OnboardingFragment
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import java.util.ArrayList
import java.util.Arrays

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button
    private lateinit var indicator: SpringDotsIndicator
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }
    private val TAG = "SplashScreenActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        nextButton = findViewById(R.id.btnNext)
        indicator = findViewById(R.id.dotsIndicator)

        val fragments = listOf(
            OnboardingFragment.newInstance(
                1,
                "Selamat datang",
                "AbzennDW adalah aplikasi presensi yang menjamin keamanan tinggi! Untuk memastikan kehadiran yang sah, Anda harus melewati tiga lapisan verifikasi: cek lokasi, scan QR code, dan verifikasi wajah. Praktis, akurat, dan anti-kecurangan!"
            ),
            OnboardingFragment.newInstance(
                2,
                "Cara presensi",
                "",
                true
            ),
            OnboardingFragment.newInstance(
                3,
                "Mulai sekarang!",
                "Masuk untuk mulai presensi dengan aman dan mudah! Cepat & Praktis, Aman & Terpercaya, dan tanpa Ribet. Masuk sekarang dan nikmati pengalaman presensi yang modern!",
                false,
                true
            )
        )

        val adapter = OnboardingPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        indicator.attachTo(viewPager)

        nextButton.setOnClickListener {
            if (viewPager.currentItem < fragments.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                // Navigate to login or main activity
                createSignInIntent()
//                finish()
            }
        }

        // Update button text based on page position
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nextButton.text = if (position == fragments.size - 1) "Mulai" else "Lanjut"
            }
        })
    }

    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers: List<AuthUI.IdpConfig> = Arrays.asList(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        // Create and launch sign-in intent
        val signInIntent: Intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
//            val intent = Intent(this, HomeActivity::class.java)
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("FIREBASE", "onSignInResult: " + user!!.email)
            //create session
//            PreferenceUtils.saveUsername(this, user.displayName)
            val sendEmail= Intent(this, HomeActivity::class.java)
            sendEmail.putExtra("email", user.email)
            Email.email= user.email
            startActivity(sendEmail)
            //check embeddings
            val embeddingReference = Utils.getFirebaseEmbedding()
            embeddingReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val embedingFloatList: MutableList<String> = ArrayList()
                        for (dataEmbeding in snapshot.children) {
                            val data = dataEmbeding.value.toString()
                            embedingFloatList.add(data)
                        }
                        PreferenceUtils.saveFaceEmbeddings(applicationContext, embedingFloatList)
                    }
                    startActivity(intent)
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed, how to handle?
                    Log.e(TAG, "onCancelled: " + error.message)
                    finish()
                }
            })
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Gagal melakukan login ", Toast.LENGTH_LONG).show();
        }
    }
}