package com.example.demo.data

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

data class Post(
    @DocumentId
    val id: String = "",
    val postDesc: String? = null,
    val img: Blob = Blob.fromBytes(ByteArray(0)), // Store Uri as String
    val video: String? = null, // Store Uri as String
    val postDate: Timestamp? = null,
    val postOwnerId: String = ""
)

data class Comment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val commentOwnerId: String = "",
    val commentDesc: String? = null,
    val commentDate: Timestamp? = null
)

val POSTS = Firebase.firestore.collection("posts")
val COMMENTS = Firebase.firestore.collection("comments")



