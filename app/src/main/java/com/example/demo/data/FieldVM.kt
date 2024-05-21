package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class FieldVM: ViewModel() {
    private val fieldVM = MutableLiveData<List<User>>(emptyList())
    private var listener: ListenerRegistration? = null

    init {
        listener = FIELDS.addSnapshotListener { snap, _ ->
            fieldVM.value = snap?.toObjects()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getUserLD() = fieldVM

    fun getAll() = fieldVM.value ?: emptyList()

    fun get(id: String) = getAll().find { it.id == id }
    fun getName(id: String) = get(id)?.name ?: ""

    fun set(user: User) {
        USERS.document(user.id).set(user);
    }
}