package com.example.demo.util

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.data.Post
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.ItemForumBinding
import android.content.res.ColorStateList
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    val fn: (ViewHolder, Post) -> Unit = { _, _ -> }
) : ListAdapter<Post, PostAdapter.ViewHolder>(DiffCallback) {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(a: Post, b: Post) = a.id == b.id
        override fun areContentsTheSame(a: Post, b: Post) = a == b
    }

    class ViewHolder(val binding: ItemForumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemForumBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        holder.binding.postImg.setImageBlob(post.img)
        holder.binding.forumDesc.text = post.postDesc
        holder.binding.postDateTime.text = post.postDate?.let {
            formatTimestamp(it)
        } ?: "Unknown date"

        fetchUserInfo(post.postOwnerId) { user ->
            holder.binding.postOwner.text = user?.name ?: "Unknown User"
            user?.photo?.let { holder.binding.ivProfile.setImageBlob(it) }
        }

        val sharedPref = holder.itemView.context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)

        if (currentUserId != null) {
            db.collection("like")
                .whereEqualTo("postId", post.id)
                .get()
                .addOnSuccessListener { documents ->
                    var exist = false
                    val likeCount = documents.size()
                    for (document in documents) {
                        if (document["ownerId"] == currentUserId) {
                            exist = true
                            break
                        }
                    }
                    if (exist) {
                        // The post is liked by the current user, change icon tint color to red
                        holder.binding.btnLike.setIconTint(ColorStateList.valueOf(Color.RED))
                        holder.binding.btnLike.setTextColor(Color.RED)
                        holder.binding.btnLike.text = "Liked"
                    } else {
                        // The post is not liked by the current user, change icon tint color to black
                        holder.binding.btnLike.setIconTint(ColorStateList.valueOf(Color.WHITE))
                        holder.binding.btnLike.setTextColor(Color.WHITE)
                        if(likeCount == 0) {
                            holder.binding.btnLike.text = "Like"
                        } else {
                            holder.binding.btnLike.text = likeCount.toString() + " Likes"
                        }
                    }
                }
        }

        if(post.postOwnerId == currentUserId) {
            holder.binding.btnEditPost.visibility = ViewGroup.VISIBLE
            holder.binding.btnDelete.visibility = ViewGroup.VISIBLE
        } else {
            holder.binding.btnEditPost.visibility = ViewGroup.GONE
            holder.binding.btnDelete.visibility = ViewGroup.GONE
        }
        fn(holder, post)
    }

    private fun fetchUserInfo(ownerId: String, callback: (User?) -> Unit) {
        USERS.document(ownerId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
    }

}