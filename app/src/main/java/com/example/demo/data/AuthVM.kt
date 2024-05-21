package com.example.demo.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthVM (val app: Application) : AndroidViewModel(app) {

    private val USERS = Firebase.firestore.collection("users")
    private val userLD = MutableLiveData<User?>()
    private var listener: ListenerRegistration? = null

    init {
        userLD.value = null
    }

    // ---------------------------------------------------------------------------------------------

    fun init() = Unit

    fun getUserLD() = userLD

    fun getUser() = userLD.value

    // TODO(1): Login
    suspend fun login(email: String, password:String, remember: Boolean = false) : Boolean{
        if (email == "" || password == "") return false

        val user = USERS
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .await()
            .toObjects<User>()
            .firstOrNull() ?: return false

        listener?.remove()
        listener = USERS.document(user.id).addSnapshotListener { snap, _ ->  userLD.value = snap?.toObject() }

        // TODO(6A): Handle remember-me -> add shared preferences
        if (remember) {
            getPreferences()
                .edit()
                .putString("email", email)
                .putString("password", password)
                .apply()
        }

        return true
    }

    // TODO(2): Logout
    fun logout() {
        // TODO(2A): Remove snapshot listener
        //           Update live data -> null
        listener?.remove()
        userLD.value = null

        // TODO(6B): Handle remember-me -> clear shared preferences
        getPreferences()
            .edit()
            .remove("email")
            .remove("password")
            .apply()

        // [OR] getPreferences().edit().clear().apply()
        // [OR] app.deleteSharedPreferences("AUTH")
    }

    // TODO(6): Get shared preferences
    private fun getPreferences() = app.getSharedPreferences("AUTH", Context.MODE_PRIVATE)

    // TODO(7): Auto login from shared preferences
    suspend fun loginFromPreferences() {
        val email = getPreferences().getString("email", null)
        val password = getPreferences().getString("password", null)


        if (email != null && password != null) {
            login(email, password)
        }
    }

}