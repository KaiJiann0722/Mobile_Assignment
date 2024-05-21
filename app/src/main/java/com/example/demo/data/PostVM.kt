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
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    fun set(postId: String, post: Post) { //updatePost
        POSTS.document(postId).set(post);
    }

    fun addLike(postId: String, ownerId: String) {
        val like = mapOf("postId" to postId, "ownerId" to ownerId)
        db.collection("like").add(like)
    }

    fun removeLike(likeId: String) {
        db.collection("like").document(likeId).delete()
    }


    fun add(post: Post) { //addPost
        POSTS.add(post)
    }

    fun delete(postId: String) { //deletePost
        val batch = db.batch()

        // Delete the post
        val postRef = POSTS.document(postId)
        batch.delete(postRef)

        // Delete the comments associated with the post
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    batch.delete(document.reference)
                }

                // Delete the likes associated with the post
                db.collection("like")
                    .whereEqualTo("postId", postId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            batch.delete(document.reference)
                        }

                        // Commit the batch
                        batch.commit().addOnSuccessListener {
                            Log.d("PostVM", "Post, associated comments and likes successfully deleted!")
                        }.addOnFailureListener { e ->
                            Log.w("PostVM", "Error deleting post, associated comments and likes", e)
                        }
                    }
            }

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