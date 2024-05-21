package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.FieldVM
import com.example.demo.data.FriendsVM
import com.example.demo.databinding.FragmentFriendsBinding
import com.example.demo.util.FriendListAdapter

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    private val nav by lazy { findNavController() }
    private val friendsVM: FriendsVM by activityViewModels()
    private val fieldVM: FieldVM by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        val adapter = FriendListAdapter { h, f ->
            h.binding.root.setOnClickListener { detail(f.id) }
        }
        binding.rvFriends.adapter = adapter
        binding.rvFriends.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId != null) {
            // Use the user ID to fetch friends
            friendsVM.fetchFriends(userId)
            friendsVM.fetchNewFriends(userId)
            friendsVM.fetchRequests(userId)
        } else {
            // Handle the case where the user ID is not found, e.g., redirect to login
            nav.navigate(R.id.loginFragment)
        }

        fieldVM.init()

        friendsVM.getResultLD().observe(viewLifecycleOwner) { friends ->
            binding.txtCount.text = "${friends.size} Friend(s)"
            adapter.submitList(friends)
        }


        binding.sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String) = true
            override fun onQueryTextChange(name: String): Boolean {

                // Change the button color when selected
                binding.btnAll.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button_color) // replace with your selected color resource
                binding.btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // replace with your selected text color resource

                // Reset the other button's color
                binding.btnOnline.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unactive_button_color) // replace with your unselected color resource
                binding.btnOnline.setTextColor(ContextCompat.getColor(requireContext(), R.color.unactive_text_color)) // replace with your unselected text color resource

                friendsVM.search(name)
                return true
            }
        })

        binding.btnOnline.setOnClickListener {
            friendsVM.sortOnline()

            // Change the button color when selected
            binding.btnOnline.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button_color) // replace with your selected color resource
            binding.btnOnline.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // replace with your selected text color resource

            // Reset the other button's color
            binding.btnAll.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unactive_button_color) // replace with your unselected color resource
            binding.btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.unactive_text_color)) // replace with your unselected text color resource
        }

        binding.btnAll.setOnClickListener{
            friendsVM.updateResult()


            // Change the button color when selected
            binding.btnAll.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button_color) // replace with your selected color resource
            binding.btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // replace with your selected text color resource

            // Reset the other button's color
            binding.btnOnline.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unactive_button_color) // replace with your unselected color resource
            binding.btnOnline.setTextColor(ContextCompat.getColor(requireContext(), R.color.unactive_text_color)) // replace with your unselected text color resource

        }

        binding.btnNew.setOnClickListener {
            findNavController().navigate(R.id.newFriendFragment)
        }

        return binding.root
    }

    private fun detail(userId: String) {
        nav.navigate(R.id.friendDetailsFragment, bundleOf("userId" to userId))
    }


}