package com.project.chatapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.project.chatapp.R
import com.project.chatapp.adapter.UserAdapter
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {
    var userList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        userRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        getUsersList()
    }

    fun getUsersList() {
        var firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        var databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

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