package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.FriendsVM
import com.example.demo.data.RecommendVM
import com.example.demo.databinding.FragmentNewFriendBinding
import com.example.demo.util.NewFriendAdapter
import com.example.demo.util.toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class NewFriendFragment : Fragment() {
    private lateinit var binding: FragmentNewFriendBinding
    private val nav by lazy { findNavController() }
    private val friendsVM: FriendsVM by activityViewModels()
    private val RecommendVM: RecommendVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewFriendBinding.inflate(inflater, container, false)


        val adapter = NewFriendAdapter { h, f ->
            h.binding.root.setOnClickListener { detail(f.id) }
            h.binding.btnAddFriend.setOnClickListener{sendFriendRequest(f.id)}
        }
        binding.rvAddFriend.adapter = adapter
        binding.rvAddFriend.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId != null) {
            // Use the user ID to fetch friends
            friendsVM.fetchFriends(userId)
            friendsVM.fetchNewFriends(userId)
            friendsVM.fetchRequests(userId)
            RecommendVM.fetchNewFriends(userId)
        } else {
            // Handle the case where the user ID is not found, e.g., redirect to login
            nav.navigate(R.id.loginFragment)
        }

        friendsVM.getNewFriendResultLD().observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        binding.sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String) = true
            override fun onQueryTextChange(name: String): Boolean {
                friendsVM.searchNewFriend(name)
                return true
            }
        })

        binding.btnQR.visibility = View.GONE
        binding.btnRecommend.visibility = View.GONE
        binding.btnOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expand_more,0,0, 0)

        binding.btnOption.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            if(binding.btnQR.visibility == View.VISIBLE){
                binding.btnQR.visibility = View.GONE
                binding.btnRecommend.visibility = View.GONE
                binding.btnOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expand_more,0,0, 0)
            }
            else{
                binding.btnQR.visibility = View.VISIBLE
                binding.btnRecommend.visibility = View.VISIBLE
                binding.btnOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expand_less,0,0, 0)
            }

        }


        binding.btnRequest.setOnClickListener {
            findNavController().navigate(R.id.friendRequestFragment)
        }
        binding.btnRecommend.setOnClickListener {
            findNavController().navigate(R.id.recommendFriendFragment)
        }

        binding.btnQR.setOnClickListener {
            nav.navigate(R.id.addFriendQRFragment, bundleOf("userId" to userId))
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun detail(userId: String) {
        nav.navigate(R.id.addFriendDetailsFragment, bundleOf("userId" to userId))
    }

    private fun sendFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.sendRequest(it, friendId))
                toast("Friend request sent")
            else
                toast("Friend request is pending, please wait the user to accept it")
        }
    }

}