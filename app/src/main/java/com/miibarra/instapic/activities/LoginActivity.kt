package com.miibarra.instapic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.miibarra.instapic.R
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if user is already logged in
        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            startPostsActivity()
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Start Firebase authentication
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successful Login!", Toast.LENGTH_SHORT).show()
                    startPostsActivity()
                } else {
                    Log.e(TAG, "signInWithEmailAndPassword failed", task.exception)
                    Toast.makeText(this, "Incorrect email/password combination", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startPostsActivity() {
        Log.i(TAG, "Starting Posts Activity")
        val postsIntent = Intent(this, PostsActivity::class.java)
        startActivity(postsIntent)
        finish()
    }
}