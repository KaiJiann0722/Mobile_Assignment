package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.Chat
import com.example.demo.data.ChatVM
import com.example.demo.data.FieldVM
import com.example.demo.data.FriendsVM
import com.example.demo.databinding.FragmentRequestFriendDetailsBinding
import com.example.demo.util.toast
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class RequestFriendDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRequestFriendDetailsBinding
    private val nav by lazy { findNavController() }
    private val userId by lazy { arguments?.getString("userId") ?: "" }
    private val fieldVM: FieldVM by activityViewModels()
    private val chatVM: ChatVM by activityViewModels()

    private val friendsVM: FriendsVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRequestFriendDetailsBinding.inflate(inflater, container, false)

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        var currentUserId = sharedPref.getString("userId", null)

        if (currentUserId != null) {
            // Use the user ID to fetch friends
            friendsVM.fetchFriends(currentUserId)
            friendsVM.fetchNewFriends(currentUserId)
            friendsVM.fetchRequests(currentUserId)
        } else {
            // Handle the case where the user ID is not found, e.g., redirect to login
            nav.navigate(R.id.loginFragment)
        }

        if(currentUserId == null) {
            currentUserId = ""
        }

        binding.btnBack.setOnClickListener { nav.navigateUp() }
        val user = friendsVM.get(userId)
        if (user == null) {
            nav.navigateUp()
            return null
        }
        binding.imgProfile.setImageResource(R.drawable.horse)
        //binding.imgProfile.setImageBlob(user.photo)
        binding.txtName.text = user.name
        binding.txtFriendEmail.text = user.email
        binding.txtGender.text = user.gender
        var bio = ""
        if (user.bio != "") {
            bio = "\"" + user.bio + "\""
        }else{
            bio = "This user is too lazy to write a bio."
        }
        binding.txtBio.text = "\"" + bio + "\""

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.format(user.dateCreated.toDate())
        binding.txtUserSince.text = date

        binding.txtField.text = fieldVM.getName(user.fieldID)

        binding.txtMutualFriends.text = friendsVM.getMutualFriends(currentUserId, user.id)


        binding.btnAcceptRequest.setOnClickListener{
            acceptFriendRequest(user.id)
            nav.navigateUp()
        }
        binding.btnDeleteRequest.setOnClickListener{
            deleteFriendRequest(user.id)
            nav.navigateUp()
        }

        return binding.root
    }

    private fun acceptFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.acceptFriendRequest(it, friendId)){
                toast("Friend request accepted")

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
    private fun deleteFriendRequest(friendId: String) {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        currentUserId?.let {
            if(friendsVM.deleteFriendRequest(it, friendId))
                toast("Friend request rejected")
        }
    }
}