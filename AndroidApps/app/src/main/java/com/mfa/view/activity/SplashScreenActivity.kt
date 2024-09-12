package com.mfa.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mfa.databinding.ActivitySplashScreenBinding
import com.mfa.`object`.Email
import com.mfa.utils.PreferenceUtils
import com.mfa.utils.Utils
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val TAG = "SplashScreenActivity"
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createSignInIntent()
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