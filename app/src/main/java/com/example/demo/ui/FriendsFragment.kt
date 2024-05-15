package com.example.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.UserVM
import com.example.demo.databinding.FragmentFriendsBinding
import com.example.demo.util.UserAdapter

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    private val nav by lazy { findNavController() }
    private val userVM: UserVM by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        val adapter = UserAdapter { h, f ->
            h.binding.root.setOnClickListener { detail(f.id) }
        }
        binding.rvFriends.adapter = adapter
        binding.rvFriends.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        userVM.getResultLD().observe(viewLifecycleOwner) { userList ->
            binding.txtCount.text = "${userList.size} Record(s)"
            adapter.submitList(userList)
        }

        return binding.root
    }

    private fun detail(userId: String) {
        nav.navigate(R.id.friendDetailsFragment, bundleOf("userId" to userId))
    }
}