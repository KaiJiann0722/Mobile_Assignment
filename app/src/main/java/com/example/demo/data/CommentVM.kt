package com.example.demo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class CommentVM: ViewModel(){
    private val commentLD = MutableLiveData<List<Comment>>((emptyList()))
    private var listener: ListenerRegistration? = null

    init {
        listener = COMMENTS.addSnapshotListener { snap, _ ->
            commentLD.value = snap?.toObjects()
            updateResult()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getCommentLD() = commentLD

    fun getAll() = commentLD.value?.sortedByDescending { it.commentDate } ?: emptyList()

    fun get(commentId: String) = getAll().find { it.id == commentId }

    fun set(comment: Comment) { //updateComment
        COMMENTS.document(comment.id).set(comment);
    }

    fun add(comment: Comment) { //addComment
        COMMENTS.add(comment)
    }

    fun delete(commentId: String) { //deleteComment
        COMMENTS.document(commentId).delete()
    }

    private val resultLD = MutableLiveData<List<Comment>>()
    fun getResultLD() = resultLD

    fun updateResult() {
        var list = getAll()
        resultLD.value = list
    }
}