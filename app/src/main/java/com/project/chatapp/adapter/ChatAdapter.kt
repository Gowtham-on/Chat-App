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

class ChatAdapter(
    private val context: Context,
    private val chatList: ArrayList<Chat>,
    private val profileUri: String,
    private val userProfile: String
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private var firebaseUser: FirebaseUser? = null
    private var side: Int = 0


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
        if (side == 1 && profileUri != "") {
            Glide.with(context).load(profileUri).into(holder.imgUser)
        } else if (side == 0 && userProfile != ""){
            Glide.with(context).load(userProfile).into(holder.imgUser)
        } else {
            Glide.with(context).load(R.drawable.profile_image).into(holder.imgUser)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsername: TextView = view.findViewById(R.id.tvMessage)
        val imgUser: CircleImageView = view.findViewById(R.id.usrImg)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (chatList[position].senderId == firebaseUser!!.uid) {
            side = 0
            return MESSAGE_TYPE_RIGHT
        } else {
            side = 1
            return MESSAGE_TYPE_LEFT
        }
    }
}