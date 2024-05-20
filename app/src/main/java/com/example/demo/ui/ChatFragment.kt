package com.example.demo.ui

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
import com.example.demo.data.FriendsVM
import com.example.demo.databinding.FragmentChatBinding
import com.example.demo.util.UserAdapter
import com.example.demo.util.setImageBlob

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val nav by lazy { findNavController() }
    private val userId by lazy { arguments?.getString("userId") ?: "" }

    private val userVM: FriendsVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        val user= userVM.get(userId)
        if (user == null) {
            nav.navigateUp()
            return null
        }
        binding.imageProfilePhoto.setImageBlob(user.photo)
        binding.txtName.text = user.name

        return binding.root
    }

}