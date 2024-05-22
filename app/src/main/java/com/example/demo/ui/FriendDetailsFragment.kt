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
import com.example.demo.data.FieldVM
import com.example.demo.data.FriendsVM
import com.example.demo.databinding.FragmentFriendDetailsBinding
import com.example.demo.util.setImageBlob
import java.text.SimpleDateFormat
import java.util.Locale


class FriendDetailsFragment : Fragment() {

    private lateinit var binding: FragmentFriendDetailsBinding
    private val nav by lazy { findNavController() }
    private val userId by lazy { arguments?.getString("userId") ?: "" }
    private val fieldVM: FieldVM by activityViewModels()

    private val friendsVM: FriendsVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailsBinding.inflate(inflater, container, false)

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

        binding.imgProfile.setImageBlob(user.photo)
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

        binding.btnDeleteFriend.setOnClickListener{
            friendsVM.deleteFriend(currentUserId, user.id)
            nav.navigateUp()
        }

        return binding.root
    }

}