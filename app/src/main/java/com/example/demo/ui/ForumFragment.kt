package com.example.demo.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.example.demo.data.PostVM
import com.example.demo.databinding.FragmentForumBinding
import com.example.demo.databinding.ItemForumBinding
import com.example.demo.util.PostAdapter
import com.example.demo.util.showConfirmationDialog

class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding
    private val nav by lazy { findNavController() }
    private val postVM: PostVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)
        binding.addPostButton.setOnClickListener {
            nav.navigate(R.id.postFragment)
        }

        val adapter = PostAdapter { h, p ->
            h.binding.btnComment.setOnClickListener { commentDetail(p.id) }
            h.binding.btnDelete.setOnClickListener {
                showConfirmationDialog("Delete Post", "Are you sure you want to delete this post?") {
                    postVM.delete(p.id)
                }
            }
        }

        binding.forumRecyclerView.adapter = adapter
        binding.forumRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        // TODO(13): Change to result live data
        postVM.getResultLD().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.sv.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String) = true
            override fun onQueryTextChange(name: String): Boolean {
                postVM.search(name)
                return true
            }
        })

        return binding.root
    }

    private fun commentDetail(postId: String) {
        nav.navigate(
            R.id.commentFragment, bundleOf(
                "postId" to postId
            )
        )
    }
}