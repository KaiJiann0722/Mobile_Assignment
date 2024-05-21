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

class MessageAdapter(context: Context) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val currentUserId: String?
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    companion object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
        override fun areContentsTheSame(a: Message, b: Message) = a == b
    }

    init {
        val sharedPref = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        currentUserId = sharedPref?.getString("userId", null)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.binding.apply {
                messageText.text = message.message
                messageTime.text = message.date?.let { formatTimestamp(it) } ?: "Unknown date"
            }
        } else if (holder is ReceivedMessageViewHolder) {
            holder.binding.apply {
                messageText.text = message.message
                messageTime.text = message.date?.let { formatTimestamp(it) } ?: "Unknown date"
            }
        }
    }

    class SentMessageViewHolder(val binding: ItemMessageSendBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedMessageViewHolder(val binding: ItemMessageReceiveBinding) : RecyclerView.ViewHolder(binding.root)
}

