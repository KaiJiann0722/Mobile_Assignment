package com.example.demo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.FriendsVM
import com.example.demo.databinding.FragmentFriendDetailsBinding


class FriendDetailsFragment : Fragment() {

    private lateinit var binding: FragmentFriendDetailsBinding
    private val nav by lazy { findNavController() }
    private val userId by lazy { arguments?.getString("userId") ?: "" }

    private val friendsVM: FriendsVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailsBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener { nav.navigateUp() }
        val user = friendsVM.get(userId)
        if (user == null) {
            nav.navigateUp()
            return null
        }
        binding.imgProfile.setImageResource(R.drawable.horse)
        //binding.imgProfile.setImageBlob(user.photo)
        binding.txtName.text = user.name
        binding.txtStatus.text = user.status


        return binding.root
    }

}