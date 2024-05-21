package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import com.google.firebase.Timestamp
import android.transition.AutoTransition
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.data.Chat
import com.example.demo.data.ChatVM
import com.example.demo.data.FriendsVM
import com.example.demo.data.NewFriendVM
import com.example.demo.databinding.FragmentFriendRequestBinding
import com.example.demo.util.RequestAdapter
import com.example.demo.util.toast


class FriendRequestFragment : Fragment() {
    private lateinit var binding: FragmentFriendRequestBinding
    private val nav by lazy { findNavController() }
    private val friendsVM: FriendsVM by activityViewModels()
    private val chatVM: ChatVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false)


        val adapter = RequestAdapter() { h, f ->
            h.binding.root.setOnClickListener { detail(f.id) }
            h.binding.btnAccept.setOnClickListener{acceptFriendRequest(f.id)}
            h.binding.imgDelete.setOnClickListener{deleteFriendRequest(f.id)}
        }
        binding.rvRequest.adapter = adapter
        binding.rvRequest.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId != null) {
            // Use the user ID to fetch friends
            friendsVM.fetchFriends(userId)
            friendsVM.fetchNewFriends(userId)
            friendsVM.fetchRequests(userId)
        } else {
            // Handle the case where the user ID is not found, e.g., redirect to login
            nav.navigate(R.id.loginFragment)
        }

        friendsVM.getRequestFromLD().observe(viewLifecycleOwner) { requests ->
            binding.txtCount.text = "${requests.size} Request(s)"
            adapter.submitList(requests)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // We don't want to support move operation in this case
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val friendRequest = adapter.currentList[position]
                if (direction == ItemTouchHelper.LEFT) {
                    // Handle left swipe
                    deleteFriendRequest(friendRequest.id)
                    adapter.notifyItemRemoved(position)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    acceptFriendRequest(friendRequest.id)
                    adapter.notifyItemRemoved(position)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvRequest)


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun detail(userId: String) {
        nav.navigate(R.id.requestFriendDetailsFragment, bundleOf("userId" to userId))
    }

    private fun acceptFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.acceptFriendRequest(it, friendId)){
                toast("Friend request accepted")

                if(!chatVM.checkIfChatExist(currentUserId, friendId)){
                    val chat = Chat(
                        participants1 = currentUserId,
                        participants2 = friendId,
                        lastMessage= "",
                        date = Timestamp.now()
                    )
                    chatVM.add(chat)
                }
            }
        }
    }
    private fun deleteFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.deleteFriendRequest(it, friendId)){
                val transition: Transition = Slide(Gravity.START)
                TransitionManager.beginDelayedTransition(binding.rvRequest, transition)
                toast("Friend request rejected")
            }
        }
    }

}