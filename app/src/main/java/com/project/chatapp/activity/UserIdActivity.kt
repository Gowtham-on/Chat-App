package com.project.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.chatapp.R
import com.project.chatapp.adapter.ChatAdapter
import com.project.chatapp.model.Chat
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_user_id.*
import java.util.*
import kotlin.collections.ArrayList

class UserIdActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var databaseReference: DatabaseReference

    private var idList = ArrayList<String>()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_id)

        Log.d("checkin", "in UserIdActivity")

        auth = FirebaseAuth.getInstance()
        btnSignup.setOnClickListener {
            progressBarInLogin.visibility = View.VISIBLE
            isPresent(userId.text.toString())
        }
    }

    private fun isPresent(currentUserId: String) {

        var userName:String? = intent.getStringExtra("userName")
        var email:String? = intent.getStringExtra("email")
        var password:String? = intent.getStringExtra("password")

        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    Log.d("checkin", user!!.userName)
                    idList!!.add(user!!.currentId!!)
                }
                Log.d("checkin", "inside")

                if (idList.contains(currentUserId)) {
                    Toast.makeText(this@UserIdActivity, "UserId already exists", Toast.LENGTH_SHORT)
                        .show()
                    progressBarInLogin.visibility = View.GONE
                } else {
                    Log.d("checkin", "present")
                    userId.setText("")
                    registerUser(userName!!, email!!, password!!, currentUserId)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                progressBarInLogin.visibility = View.GONE
            }
        })


    }
    private fun registerUser(userName: String, email: String, password: String, currentId: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    val userId: String = user!!.uid

                    databaseReference =
                        FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashMap: HashMap<String, String> = HashMap()

                    hashMap["userId"] = userId
                    hashMap["userName"] = userName
                    hashMap["profileImage"] = ""
                    hashMap["storageProf"] = ""
                    hashMap["currentId"] = currentId

                    databaseReference.setValue(hashMap).addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            progressBarInLogin.visibility = View.GONE
                            val intent = Intent(this@UserIdActivity, UserActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(applicationContext, "failed", Toast.LENGTH_SHORT).show()
                            progressBarInLogin.visibility = View.GONE
                        }
                    }
                }
            }
    }
}