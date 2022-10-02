package com.project.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.chatapp.R
import com.project.chatapp.activity.ChatActivity
import com.project.chatapp.model.Chat
import com.project.chatapp.model.User
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val context: Context, private val chatList: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private var firebaseUser: FirebaseUser? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
            return ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
            return ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.txtUsername.text = chat.message
//        Glide.with(context).load(chat.profileImage).placeholder(R.drawable.profile_image).into(holder.imgUser)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsername: TextView = view.findViewById(R.id.tvMessage)
        val imgUser: CircleImageView = view.findViewById(R.id.userImage)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (chatList[position].senderId == firebaseUser!!.uid) {
            return MESSAGE_TYPE_RIGHT
        } else {
            return MESSAGE_TYPE_LEFT
        }
    }

}