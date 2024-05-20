package com.example.demo.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.data.Comment
import com.example.demo.data.Post
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.ItemCommentBinding
import com.google.firebase.Timestamp

class CommentAdapter(
    val fn: (ViewHolder, Comment) -> Unit = { _, _ -> }
) : ListAdapter<Comment, CommentAdapter.ViewHolder>(DiffCallback)  {
    companion object DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(a: Comment, b: Comment) = a.id == b.id
        override fun areContentsTheSame(a: Comment, b: Comment) = a == b
    }
    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val comment = getItem(position)
        fetchUserInfo(comment.commentOwnerId) { user ->
            holder.binding.commentName.text = user?.name ?: "Unknown User"
            user?.photo?.let { holder.binding.commentProfilePicture.setImageBlob(it) }
        }
        holder.binding.commentDescription.text = comment.commentDesc
        holder.binding.commentTime.text = comment.commentDate?.let { formatTimestamp(it) }.toString()
        fn(holder, comment)
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

    override fun getItemCount(): Int {
        return currentList.size
    }

}