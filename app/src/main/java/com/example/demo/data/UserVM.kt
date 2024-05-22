package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase


class UserVM : ViewModel() {
    private val userLD = MutableLiveData<List<User>>(emptyList())
    private val users = Firebase.firestore.collection("users")
    private var listener: ListenerRegistration? = null

    init {
        listener = users.addSnapshotListener { snap, _ ->
            userLD.value = snap?.toObjects()
            updateResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getUserLD() = userLD

    fun getAll() = userLD.value ?: emptyList()

    fun get(id: String) = getAll().find { it.id == id }

    fun set(user: User) {
        users.document(user.id).set(user)
            .addOnSuccessListener {
                println("User updated successfully in Firestore.")
            }
            .addOnFailureListener { exception ->
                println("Failed to update user in Firestore: ${exception.message}")
            }
    }


    private val resultLD = MutableLiveData<List<User>>()
    private var name = ""
    fun getResultLD() = resultLD
    fun search(name: String) {
        this.name = name
        updateResult()
    }

    fun updateResult() {
        var list = getAll()

        // TODO(12A): Search by name, filter by categoryId
        list = list.filter {
            it.name.contains(name, true)
        }

        resultLD.value = list
    }


    private fun emailExists(email: String) = userLD.value?.any { it.id == email } ?: false

    fun validate(user: User, insert: Boolean = true): String {
        val regexEmail = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$""")
        var e = ""

        if (insert) {
            e += if (user.email == "") "- Email required.\n"
            else if (!user.email.matches(regexEmail)) "- Email format invalid.\n"
            else if (user.email.length > 100) "- Email too long (max 100 chars).\n"
            else if (emailExists(user.email)) "- Email duplicated.\n"
            else ""
        }

        e += if (user.password == "") "- Password required.\n"
        else if (user.password.length < 5) "- Password too short (min 5 chars).\n"
        else if (user.password.length > 100) "- Password too long (max 100 chars).\n"
        else ""

        e += if (user.name == "") "- Name required.\n"
        else if (user.name.length < 3) "- Name too short (min 3 chars).\n"
        else if (user.name.length > 100) "- Name too long (max 100 chars).\n"
        else ""

        e += if (user.photo.toBytes().isEmpty()) "- Photo required.\n"
        else ""

        return e
    }

    fun updateStatus(id: String, status: String) {
        val user = get(id) ?: return
        user.status = status
        set(user)
    }
}