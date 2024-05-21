package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.data.FriendsVM
import com.example.demo.data.RecommendVM
import com.example.demo.databinding.FragmentAddFriendQRBinding
import com.example.demo.databinding.FragmentRecommendFriendBinding
import com.example.demo.util.NewFriendAdapter
import com.example.demo.util.RecommendFriendAdapter
import com.example.demo.util.toast


class RecommendFriendFragment : Fragment() {
    private lateinit var binding: FragmentRecommendFriendBinding
    private val nav by lazy { findNavController() }
    private val recommendVM: RecommendVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecommendFriendBinding.inflate(inflater, container, false)

        val layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        binding.rvRecommend.layoutManager = layoutManager


        val adapter = RecommendFriendAdapter { h, f ->
            h.binding.btnMatch.setOnClickListener{sendFriendRequest(f.id)}
            h.binding.btnSkip.setOnClickListener{skip(f.id)}
            if(f.gender == "Male"){
                h.binding.icBoy.visibility = View.VISIBLE
                h.binding.icGirl.visibility = View.GONE
            }
            else{
                h.binding.icBoy.visibility = View.GONE
                h.binding.icGirl.visibility = View.VISIBLE
            }
        }
        binding.rvRecommend.adapter = adapter
        binding.rvRecommend.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId != null) {
            // Use the user ID to fetch friends
            recommendVM.fetchNewFriends(userId)
        } else {
            // Handle the case where the user ID is not found, e.g., redirect to login
            nav.navigate(R.id.loginFragment)
        }

        recommendVM.getRecommendLD().observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
            if (users.isEmpty()) {
                updateText(true);
            } else {
                updateText(false);
            }

        }


        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // We don't want to support move operation in this case
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val friendRequest = adapter.currentList[position]
                if (direction == ItemTouchHelper.LEFT) {
                    // Handle left swipe
                    skip(friendRequest.id)
                    adapter.notifyItemRemoved(position)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    sendFriendRequest(friendRequest.id)
                    adapter.notifyItemRemoved(position)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvRecommend)

        return binding.root
    }

    private fun updateText(isEmpty: Boolean) {
        if (isEmpty) {
            binding.txtEmptyMSG.text = "Opps...No more recommendations"
        } else {
            binding.txtEmptyMSG.text = ""
        }
    }

    private fun skip(skipID: String) {
        recommendVM.skip(skipID)
        toast("Skiped")
    }

    private fun sendFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            recommendVM.sendRequest(it, friendId)
            toast("Liked")
        }
    }
}