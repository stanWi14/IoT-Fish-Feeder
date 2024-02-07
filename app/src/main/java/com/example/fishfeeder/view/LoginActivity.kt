package com.example.fishfeeder.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fishfeeder.Kosongan
import com.example.fishfeeder.R
import com.example.fishfeeder.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    lateinit var client: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLogin.setOnClickListener() {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            // Clear the previously cached account to ensure the user is prompted to select an account each time
            GoogleSignIn.getClient(this, options).signOut().addOnCompleteListener {
                val client = GoogleSignIn.getClient(this, options)
                val intent = client.signInIntent
                startActivityForResult(intent, 10001)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loginSuccess()
                        } else {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                // Handle the case where the user cancels the sign-in process or encounters an error
                Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loginSuccess() {
        // Set the flag indicating that the app has been opened
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstRun", false)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        // Move to the main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}