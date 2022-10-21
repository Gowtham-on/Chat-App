package com.project.chatapp.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.chatapp.R
import com.project.chatapp.adapter.UserAdapter
import com.project.chatapp.firebase.FirebaseService
import com.project.chatapp.model.Chat
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_user.imgBack
import kotlinx.android.synthetic.main.activity_user.imgProfile
import kotlinx.android.synthetic.main.activity_user.userRecyclerView
import kotlinx.android.synthetic.main.item_user.*
import java.util.HashMap

class UserActivity : AppCompatActivity() {
    var userList = ArrayList<User>()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var currentUserData: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this@UserActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


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

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

//        var uri:Uri? = "gs://chatapp-82c40.appspot.com/image/9194bd5d-8c3f-4f97-bb9a-faec7e5bbcda" as Uri


        userRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(this@UserActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        getUsersList()

        searchUser.setOnClickListener {
            searchUser.visibility = View.GONE
            userRecyclerView.visibility = View.GONE
            imgBack.visibility = View.GONE
            messageTextView.visibility = View.GONE
            searchIds.visibility = View.VISIBLE
            imgAddBack.visibility = View.VISIBLE
            addUser.visibility = View.VISIBLE
        }

        addUser.setOnClickListener {
            findUser(searchIds.text.toString())
        }
        imgAddBack.setOnClickListener {
            searchUser.visibility = View.VISIBLE
            userRecyclerView.visibility = View.VISIBLE
            imgBack.visibility = View.VISIBLE
            messageTextView.visibility = View.VISIBLE
            searchIds.visibility = View.GONE
            imgAddBack.visibility = View.GONE
            addUser.visibility = View.GONE
        }
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


                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (user!!.userId.equals(firebase.uid)) {
                        currentUserData = user
                    }
                }

                var list = ArrayList<String>()
                for (ids: String in currentUserData!!.friends) {
                    list.add(ids)
                }

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (list.contains(user!!.currentId)) {
                        userList.add(user)
                    }
                }

                if (currentUserData!!.storageProf == "") {
                    imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@UserActivity).load(currentUserData!!.storageProf)
                        .into(imgProfile)
                }

                val userAdapter =
                    UserAdapter(this@UserActivity, userList, currentUserData!!.storageProf!!)
                userRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun findUser(enteredId: String) {
        var firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userId = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userId")

        var databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        var found = false

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (user!!.currentId!!.equals(enteredId)) {
                        found = true
                        searchIds.setText("")

                        if (enteredId != currentUserData!!.currentId) {
                            addUsers(user)
                        }

                        var intent = Intent(this@UserActivity, ChatActivity::class.java)
                        intent.putExtra("userId", user.userId)
                        intent.putExtra("userName", user.userName)
                        intent.putExtra("profileUri", user.storageProf)
                        intent.putExtra("userProfile", user.profileImage)
                        startActivity(intent)
                        break
                    }
                }
                if (!found)
                    Toast.makeText(this@UserActivity, "Username doesnot exist", Toast.LENGTH_SHORT)
                        .show()

                searchUser.visibility = View.VISIBLE
                userRecyclerView.visibility = View.VISIBLE
                imgBack.visibility = View.VISIBLE
                messageTextView.visibility = View.VISIBLE
                searchIds.visibility = View.GONE
                imgAddBack.visibility = View.GONE
                addUser.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addUsers(user: User) {
        var firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        var databaseReferenceForLoggedUser: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(firebase.uid)
        var databaseReferenceForSender: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(user.userId)

        var friendsListForLogged: ArrayList<String> = ArrayList()
        var friendsListForSender: ArrayList<String> = ArrayList()

        friendsListForLogged = currentUserData!!.friends
        friendsListForSender = user.friends

        if (friendsListForLogged.contains(user.currentId)) {
            return
        }

        friendsListForLogged.add(user.currentId!!)
        friendsListForSender.add(currentUserData!!.currentId!!)

        Log.d("currentuserdata", friendsListForLogged.toString())

        val hashMapForLogged: HashMap<String, Any> = HashMap()
        val hashMapForSender: HashMap<String, Any> = HashMap()

        hashMapForLogged["friends"] = friendsListForLogged
        hashMapForSender["friends"] = friendsListForSender

        databaseReferenceForLoggedUser.updateChildren(hashMapForLogged as Map<String, Any>)
        databaseReferenceForSender.updateChildren(hashMapForSender as Map<String, Any>)


    }
}