package com.project.chatapp.model

import android.net.Uri

data class User(val userId: String = "",
                val userName: String = "",
                val profileImage: String = "",
                val storageProf: String? = "",
                val currentId: String? = "",
                val friends: ArrayList<String> =  ArrayList<String>()
)