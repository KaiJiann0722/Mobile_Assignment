package com.example.demo.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.demo.util.errorDialog
import com.example.demo.util.showConfirmationDialog
import com.google.firebase.firestore.FirebaseFirestore

class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding
    private val nav by lazy { findNavController() }
    private val postVM: PostVM by activityViewModels()

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)
        binding.addPostButton.setOnClickListener {
            nav.navigate(R.id.postFragment)
        }

        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)

        val adapter = PostAdapter { h, p ->
            h.binding.btnLike.setOnClickListener {
                if (currentUserId != null) {
                    db.collection("like")
                        .whereEqualTo("postId", p.id)
                        .get()
                        .addOnSuccessListener { documents ->
                            var exist = false
                            var likeID = ""
                            for (doc in documents) {
                                if (doc["ownerId"] == currentUserId) {
                                    exist = true
                                    likeID = doc.id
                                    break
                                }
                            }
                            if (exist) {
                                postVM.removeLike(likeID)
                                h.binding.btnLike.setIconTint(ColorStateList.valueOf(Color.WHITE))
                                updateLikeCountAndButtonText(p.id, h)
                            } else {
                                postVM.addLike(p.id, currentUserId)
                                h.binding.btnLike.setIconTint(ColorStateList.valueOf(Color.RED))
                                h.binding.btnLike.text = "Liked"
                            }

                        }
                } else {
                    errorDialog("Please login to like this post.")
                    nav.navigateUp()
                }
            }
            h.binding.btnComment.setOnClickListener { commentDetail(p.id) }
            h.binding.btnEditPost.setOnClickListener {
                nav.navigate(
                    R.id.postEditFragment, bundleOf(
                        "postId" to p.id
                    )
                )
            }
            h.binding.btnDelete.setOnClickListener {
                showConfirmationDialog("Delete Post", "Are you sure you want to delete this post?") {
                    postVM.delete(p.id)
                }
            }
        }

        binding.forumRecyclerView.adapter = adapter
        binding.forumRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

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

    private fun updateLikeCountAndButtonText(postId: String, h: PostAdapter.ViewHolder) {
        db.collection("like")
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { documents ->
                val likeCount = documents.size()
                if (likeCount == 0) {
                    h.binding.btnLike.text = "Like"
                } else {
                    h.binding.btnLike.text = likeCount.toString() + " Likes"
                }
            }
    }


    private fun commentDetail(postId: String) {
        nav.navigate(
            R.id.commentFragment, bundleOf(
                "postId" to postId
            )
        )
    }
}