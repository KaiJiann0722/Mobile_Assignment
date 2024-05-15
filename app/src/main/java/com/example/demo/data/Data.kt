package com.example.demo.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

data class Course(
    @DocumentId
    var id: String = "",
    var name: String = ""
) {
    @get:Exclude
    var count: Int = 0
    override fun toString() = name
}

data class User(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    var courseID: String = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0))
) {
    @get:Exclude
    var category: Course  =  Course()
}

//======================================================================================================

val COURSE = Firebase.firestore.collection("course")
val USERS = Firebase.firestore.collection("users")

//======================================================================================================

fun RESTORE(ctx: Context){

}