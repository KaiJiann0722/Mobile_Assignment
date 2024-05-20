package com.example.demo.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.data.Post
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.ItemForumBinding
import com.example.demo.ui.ForumFragment
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    val fn: (ViewHolder, Post) -> Unit = { _, _ -> }
) : ListAdapter<Post, PostAdapter.ViewHolder>(DiffCallback) {

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