package com.project.chatapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.project.chatapp.R
import com.project.chatapp.adapter.UserAdapter
import com.project.chatapp.firebase.FirebaseService
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_user.imgBack
import kotlinx.android.synthetic.main.activity_user.imgProfile
import kotlinx.android.synthetic.main.activity_user.userRecyclerView
import kotlinx.android.synthetic.main.item_user.*

class UserActivity : AppCompatActivity() {
    var userList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        val token: String = task.result!!
                        FirebaseService.token = token
                    }
                }
            }


        userRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(this@UserActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        getUsersList()
    }

    private fun getUsersList() {
        var firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userId = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userId")

        var databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                val currentUser = snapshot.getValue(User::class.java)

                if (currentUser!!.profileImage == "") {
                    imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@UserActivity).load(currentUser.profileImage).into(imgProfile)
                }

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (!user!!.userId.equals(firebase.uid)) {
                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@UserActivity, userList)
                userRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

//https://cdn.pixabay.com/photo/2015/11/16/14/43/cat-1045782_960_720.jpg
//https://cdn.pixabay.com/photo/2019/11/08/11/56/kitten-4611189_960_720.jpg