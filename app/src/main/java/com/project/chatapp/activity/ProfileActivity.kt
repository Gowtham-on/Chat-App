package com.project.chatapp.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.chatapp.R
import com.project.chatapp.model.User
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_user.imgBack
import kotlinx.android.synthetic.main.activity_user.imgProfile
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private val hashMap: HashMap<String, String> = HashMap()
    private var filePath: Uri? = null
    private var PICK_IMAGE_REQUEST: Int = 2020

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                etUserName.setText(user!!.userName)


                if (user.profileImage == "") {
                    Log.d("Checkingg", user!!.profileImage + " in if")
                    userImage.setImageResource(R.drawable.profile_image)
                } else {
                    Log.d("Checkingg", user.profileImage+"in else")
                    Log.d("Checkingg", filePath.toString() + "in else")
                    Glide.with(this@ProfileActivity).load(user.profileImage).into(userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        imgBack.setOnClickListener {
            onBackPressed()
        }

        userImage.setOnClickListener {
            chooseImage()
        }
        btnSave.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            uploadImage()
        }
//        imgProfile.setOnClickListener {
//            val intent = Intent(this@ProfileActivity, UserActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }


    private fun chooseImage() {
        var intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode != null) {
            filePath = data!!.data

            try {
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                userImage.setImageBitmap(bitmap)
                btnSave.visibility = View.VISIBLE
            } catch (err: IOException) {
                err.printStackTrace()
            }

        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            var ref: StorageReference = storageRef.child("image/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {



                    hashMap.put("userName",etUserName.text.toString())
                    hashMap.put("profileImage", filePath.toString())

                    Log.d("Checkingg", "$filePath is the path")

                    databaseReference.updateChildren(hashMap as Map<String, Any>)

                    progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    btnSave.visibility = View.GONE

                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        applicationContext,
                        "Upload Failed ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }
    }
}