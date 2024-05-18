package com.example.demo.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

data class Field(
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
    var bio: String = "",
    var email: String = "",
    var password: String = "",
    var gender: String = "",
    var fieldID: String = "",
    var status: String = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0))
) {
    @get:Exclude
    var field: Field  =  Field()
}

//======================================================================================================

val COURSE = Firebase.firestore.collection("field")
val USERS = Firebase.firestore.collection("users")

//======================================================================================================

fun RESTORE(ctx: Context){

}