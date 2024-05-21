package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.FriendsVM
import com.example.demo.data.NewFriendVM
import com.example.demo.databinding.FragmentFriendRequestBinding
import com.example.demo.util.RequestAdapter
import com.example.demo.util.toast


class FriendRequestFragment : Fragment() {
    private lateinit var binding: FragmentFriendRequestBinding
    private val nav by lazy { findNavController() }
    private val friendsVM: FriendsVM by activityViewModels()

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

//        binding.btnReceived.setOnClickListener(){
//            friendsVM.getRequestFromLD().observe(viewLifecycleOwner) { requests ->
//                adapter.setItemType(ItemType.REQUEST_RECEIVED)
//                binding.txtCount.text = "${requests.size} Request(s)"
//                adapter.submitList(requests)
//
//                // Change the button color and text color when clicked
//                binding.btnReceived.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button_color)
//                binding.btnReceived.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//                // Reset the other button's color and text color
//                binding.btnSent.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unactive_button_color)
//                binding.btnSent.setTextColor(ContextCompat.getColor(requireContext(), R.color.unactive_text_color))
//
//
//            }
//        }

//        binding.btnSent.setOnClickListener(){
//            friendsVM.getRequestToLD().observe(viewLifecycleOwner) { requests ->
//                adapter.setItemType(ItemType.REQUEST_SENT)
//                binding.txtCount.text = "${requests.size} Request(s)"
//                adapter.submitList(requests)
//
//                // Change the button color and text color when clicked
//                binding.btnSent.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button_color)
//                binding.btnSent.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//                // Reset the other button's color and text color
//                binding.btnReceived.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unactive_button_color)
//                binding.btnReceived.setTextColor(ContextCompat.getColor(requireContext(), R.color.unactive_text_color))
//
//            }
//        }


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
            if(friendsVM.acceptFriendRequest(it, friendId))
                toast("Friend request accepted")
        }
    }
    private fun deleteFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.deleteFriendRequest(it, friendId))
                toast("Friend request rejected")
        }
    }

}