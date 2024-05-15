package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects


class UserVM : ViewModel() {
    private val userLD = MutableLiveData<List<User>>(emptyList())
    private var listener: ListenerRegistration? = null

    init {
        listener = USERS.addSnapshotListener { snap, _ ->
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
}