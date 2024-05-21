package com.example.demo.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.data.User
import com.example.demo.databinding.ItemFriendsBinding


class FriendListAdapter(
    val fn: (ViewHolder, User) -> Unit = { _, _ -> }
) : ListAdapter<User, FriendListAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.id == b.id
        override fun areContentsTheSame(a: User, b: User) = a == b
    }

    class ViewHolder(val binding: ItemFriendsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        val id = user.id
        //holder.binding.imgProfile.setImageBlob(user.photo)
        holder.binding.txtName.text = user.name
        holder.binding.txtStatus.text = user.status
        fn(holder, user)
    }
}
