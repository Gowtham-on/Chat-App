package com.project.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.chatapp.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if(auth == null) {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        firebaseUser = auth.currentUser!!

        if(firebaseUser != null) {
            val intent = Intent(this@LoginActivity, UserActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            val email: String = etEmail.text.toString()
            val password: String = etPassword.text.toString()

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                Toast.makeText(
                    applicationContext,
                    "Email and Password are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            etEmail.setText("")
                            etPassword.setText("")
                            val intent = Intent(this@LoginActivity, UserActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Invalid Email and Password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        btnSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}