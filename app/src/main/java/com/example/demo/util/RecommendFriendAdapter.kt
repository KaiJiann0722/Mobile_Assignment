package com.example.demo.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.data.User
import com.example.demo.databinding.ItemAddFriendBinding
import com.example.demo.databinding.ItemRecommendFriendBinding

class RecommendFriendAdapter (
    val fn: (ViewHolder, User) -> Unit = { _, _ -> }
) : ListAdapter<User, RecommendFriendAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.id == b.id
        override fun areContentsTheSame(a: User, b: User) = a == b
    }

    class ViewHolder(val binding: ItemRecommendFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRecommendFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        val id = user.id
        holder.binding.imgRecommandProfile.setImageBlob(user.photo)
        holder.binding.txtRecommandName.text = user.name
        fn(holder, user)
    }
}