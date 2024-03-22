package com.mfa.view

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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mfa.databinding.ActivitySplashScreenBinding
import com.mfa.utils.PreferenceUtils
import java.util.*

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

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
            val intent = Intent(this, HomeActivity::class.java)
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("FIREBASE", "onSignInResult: " + user!!.email)
            PreferenceUtils.saveUsername(this, user.displayName)
            val userName = user.uid + "_" + user.displayName!!.replace(" ", "")
            val email = user.email
            intent.putExtra("email", email)
            val embeddingReference =
                FirebaseDatabase.getInstance().getReference("faceantispooflog/$userName/faceEmbeddings")
            embeddingReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val embedingFloatList: MutableList<String> = ArrayList()
                        for (dataEmbeding in snapshot.children) {
                            val data = dataEmbeding.value.toString()
                            embedingFloatList.add(data)
                        }
                        PreferenceUtils.saveFaceEmbeddings(applicationContext, embedingFloatList)
                        //todo : need to encapsulate this face registration process

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed, how to handle?
                }
            })
            startActivity(intent)
            finish()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Gagal melakukan login ", Toast.LENGTH_LONG).show();
        }
    }
}