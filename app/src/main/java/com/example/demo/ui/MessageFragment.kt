package com.example.demo.ui

import android.os.Bundle
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
import com.example.demo.databinding.FragmentChatBinding
import com.example.demo.databinding.FragmentMessageBinding
import com.example.demo.util.UserAdapter
import com.example.demo.util.setImageBlob

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private val nav by lazy { findNavController() }
    private val userVM: FriendsVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessageBinding.inflate(inflater, container, false)

        val adapter = UserAdapter { h, f ->
            h.binding.root.setOnClickListener { detail(f.id) }
        }

        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        // TODO(13): Change to result live data
        userVM.getResultLD().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.sv.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String) = true
            override fun onQueryTextChange(name: String): Boolean {
                userVM.search(name)
                return true
            }
        })
        return binding.root
    }

    private fun detail(userId: String) {
        nav.navigate(
            R.id.chatFragment, bundleOf(
                "userId" to userId
            )
        )
    }
}