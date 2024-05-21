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
import com.example.demo.data.ChatVM
import com.example.demo.data.Message
import com.example.demo.data.MessageVM
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.FragmentMessageBinding
import com.example.demo.util.MessageAdapter
import com.example.demo.util.setImageBlob
import com.google.firebase.Timestamp

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private val nav by lazy { findNavController() }
    private val messageVM: MessageVM by activityViewModels()
    private val chatId by lazy { arguments?.getString("chatId") ?: "" }
    private val chatVM: ChatVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)

        binding = FragmentMessageBinding.inflate(inflater, container, false)
        val chat = chatVM.get(chatId)
        if (chat == null || currentUserId == null) {
            nav.navigateUp()
            return null
        }
        val recepientId = if (chat.participants1 == currentUserId) chat.participants2 else chat.participants1
        fetchUserInfo(recepientId) { user ->
            binding.txtName.text = user?.name ?: "Unknown User"
            user?.photo?.let { binding.imageProfilePhoto.setImageBlob(it) }
        }

        val adapter = MessageAdapter(requireContext())
        binding.rvMessages.adapter = adapter
        binding.rvMessages.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))


        messageVM.getResultLD().observe(viewLifecycleOwner) { chats  ->
            val filteredChats = chats
                .filter { it.chatId == chatId }
            adapter.submitList(filteredChats)
        }

        binding.btnSendMessage.setOnClickListener {
            val messageText = binding.edtMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Create a new message and add it to the database
                val message = Message(
                    chatId = chatId,
                    message = messageText,
                    senderId = currentUserId,
                    date = Timestamp.now()
                )
                messageVM.add(message)
                // Clear the EditText
                binding.edtMessage.text.clear()
            }
        }

        return binding.root
    }
    private fun fetchUserInfo(ownerId: String, callback: (User?) -> Unit) {
        USERS.document(ownerId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
    }

}