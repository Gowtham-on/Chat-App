package com.project.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.chatapp.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        next.setOnClickListener {
            val userName: String = etName.text.toString()
            val email: String = etEmail.text.toString()
            val password: String = etPassword.text.toString()
            val confirmPassword: String = etConfirmPassword.text.toString()

            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(applicationContext, "UserName is required", Toast.LENGTH_SHORT)
                    .show()
            } else if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Email is required", Toast.LENGTH_SHORT)
                    .show()
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    applicationContext,
                    "Password is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(
                    applicationContext,
                    "Password Confirmation is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(
                    applicationContext,
                    "Password Confirmation is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this@SignUpActivity, UserIdActivity::class.java)
                intent.putExtra("userName", userName)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


}