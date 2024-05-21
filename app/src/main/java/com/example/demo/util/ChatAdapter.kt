package com.example.demo.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.data.Chat
import com.example.demo.data.Message
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.ItemContainerUserBinding
import com.example.demo.databinding.ItemMessageReceiveBinding
import com.example.demo.databinding.ItemMessageSendBinding

class ChatAdapter(val fn: (ViewHolder, Chat) -> Unit = { _, _ -> }
) : ListAdapter<Chat, ChatAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(a: Chat, b: Chat) = a.chatId == b.chatId
        override fun areContentsTheSame(a: Chat, b: Chat) = a == b
    }

    class ViewHolder(val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = getItem(position)
        holder.binding.txtMessage.text = chat.lastMessage
        val sharedPref = holder.itemView.context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        holder.binding.txtTime.text = chat.date?.let { formatTimestamp(it) } ?: "Unknown date"
        val participantId = if (chat.participants1 == currentUserId) chat.participants2 else chat.participants1
        fetchUserInfo(participantId) { user ->
            holder.binding.username.text = user?.name ?: "Unknown User"
            user?.photo?.let { holder.binding.profileImage.setImageBlob(it) }
        }
        fn(holder, chat)
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