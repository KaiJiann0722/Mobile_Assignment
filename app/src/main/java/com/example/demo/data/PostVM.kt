package com.example.demo.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class PostVM: ViewModel() {
    private val postLD = MutableLiveData<List<Post>>((emptyList()))
    private var listener: ListenerRegistration? = null
    init {
        listener = POSTS.addSnapshotListener { snap, _ ->
            postLD.value = snap?.toObjects()
            updateResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getPostLD() = postLD

    fun getAll() = postLD.value?.sortedByDescending { it.postDate } ?: emptyList()

    fun get(postId: String) = getAll().find { it.id == postId }

    fun set(post: Post) { //updatePost
        POSTS.document(post.id).set(post);
    }

    fun add(post: Post) { //addPost
        POSTS.add(post)
    }

    fun delete(postId: String) { //deletePost
        // TODO: Delete record by id
        POSTS.document(postId).delete()
    }


    private val resultLD = MutableLiveData<List<Post>>()
    private var name = ""
    fun getResultLD() = resultLD
    fun search(name: String) {
        this.name = name
        updateResult()
    }

    fun updateResult() {
        var list = getAll()
        resultLD.value = list
    }


}