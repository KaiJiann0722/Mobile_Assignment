package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.ChatVM
import com.example.demo.data.FriendsVM
import com.example.demo.data.MessageVM
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.FragmentChatBinding
import com.example.demo.databinding.FragmentMessageBinding
import com.example.demo.util.ChatAdapter
import com.example.demo.util.MessageAdapter
import com.example.demo.util.UserAdapter
import com.example.demo.util.setImageBlob

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val nav by lazy { findNavController() }
    private val chatVM: ChatVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        val adapter = ChatAdapter { h, f ->
            h.binding.root.setOnClickListener { detail(f.chatId) }
        }

        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)

        chatVM.getResultLD().observe(viewLifecycleOwner) { chats ->
            val filteredChats = chats.filter { chat ->
                chat.participants1 == currentUserId || chat.participants2 == currentUserId
            }
            adapter.submitList(filteredChats)
        }

        return binding.root
    }

    private fun detail(chatId: String) {
        nav.navigate(
            R.id.messageFragment, bundleOf(
                "chatId" to chatId
            )
        )
    }

}