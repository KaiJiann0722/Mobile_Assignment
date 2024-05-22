package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class MessageVM: ViewModel() {
    private val messageLD = MutableLiveData<List<Message>>((emptyList()))
    private var listener: ListenerRegistration? = null

    init {
        listener = MESSAGES.addSnapshotListener { snap, _ ->
            messageLD.value = snap?.toObjects()
            updateResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getChatLD() = messageLD

    fun getAll() = messageLD.value?.sortedBy{it.date} ?: emptyList()

    fun get(id: String) = getAll().find { it.id == id }

    fun add(message: Message) { //addChat
        MESSAGES.add(message)
    }

    fun delete(messageId: String) {
        MESSAGES.document(messageId).delete()
    }

    private val resultLD = MutableLiveData<List<Message>>()
    fun getResultLD() = resultLD

    fun updateResult() {
        var list = getAll()
        resultLD.value = list
    }
}