package com.example.fishfeeder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, options)
        binding.btnLogin.setOnClickListener() {
            val intent = client.signInIntent
            startActivityForResult(intent,10001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == 10001){
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account  = task.getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    loginSuccess()
                }else{
                    Toast.makeText(this, task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }
    }

    fun loginSuccess() {
        // shared preference is login true
        // pindah intent
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        // Move to the main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}