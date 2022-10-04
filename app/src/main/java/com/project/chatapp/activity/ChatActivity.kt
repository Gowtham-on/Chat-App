package com.project.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.project.chatapp.R
import com.project.chatapp.RetrofitInstance
import com.project.chatapp.`interface`.NotificationApi
import com.project.chatapp.adapter.ChatAdapter
import com.project.chatapp.adapter.UserAdapter
import com.project.chatapp.model.Chat
import com.project.chatapp.model.NotificationData
import com.project.chatapp.model.PushNotification
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_left.*
import kotlinx.android.synthetic.main.item_right.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser?? = null
    var databaseReference: DatabaseReference? = null
    var chatList = ArrayList<Chat>()
    var topic = ""
    var profileUri:String? = null
    var userProfile:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        var intent = getIntent()
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")
        profileUri = intent.getStringExtra("profileUri")
        userProfile = intent.getStringExtra("userProfile")

        Log.d("checkinglist",  profileUri.toString())

        firebaseUser = FirebaseAuth.getInstance().currentUser
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        imgBackInProfile.setOnClickListener {
            onBackPressed()
        }

        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                tvUserName.text = user!!.userName
                if (user.storageProf == "") {
                    imgProfileInChat.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@ChatActivity).load(user.storageProf).into(imgProfileInChat)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        btnSendMessage.setOnClickListener {
            var message = etMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "Message is empty", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(firebaseUser!!.uid, userId, message)
                etMessage.setText("")

                topic = "/topics/$userId"
                PushNotification(
                    NotificationData(userName!!, message),
                    topic
                ).also {
                    sendNotification(it)
                }

            }
        }
        readMessage(firebaseUser!!.uid, userId)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        var databaseReference: DatabaseReference? =
            FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()

        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        databaseReference!!.child("Chat").push().setValue(hashMap)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        var databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId)
                        || chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList, profileUri!!, userProfile!!)
                chatRecyclerView.adapter = chatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun sendNotification(notification: PushNotification) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Response ${Gson().toJson(response)}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ChatActivity,
                        "Response ${Gson().toJson(response)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
//                Toast.makeText(
//                    applicationContext,
//                    e.message,
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }
    }

}