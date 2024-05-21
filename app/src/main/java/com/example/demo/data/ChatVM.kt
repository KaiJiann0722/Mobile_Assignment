package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class ChatVM: ViewModel() {
    private val chatLD = MutableLiveData<List<Chat>>((emptyList()))
    private var listener: ListenerRegistration? = null

    init {
        listener = CHATS.addSnapshotListener { snap, _ ->
            chatLD.value = snap?.toObjects()
            updateResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getChatLD() = chatLD

    fun getAll() = chatLD.value?.sortedByDescending { it.date} ?: emptyList()

    fun get(chatId: String) = getAll().find { it.chatId == chatId }

    fun add(chat: Chat) { //addChat
        CHATS.add(chat)
    }

    private val resultLD = MutableLiveData<List<Chat>>()
    fun getResultLD() = resultLD

    fun updateResult() {
        var list = getAll()
        resultLD.value = list
    }
}