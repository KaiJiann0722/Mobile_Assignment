package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.demo.R
import com.example.demo.data.CommentVM
import com.example.demo.data.PostVM
import com.example.demo.data.User
import com.example.demo.data.USERS
import com.example.demo.databinding.FragmentCommentBinding
import com.example.demo.util.CommentAdapter
import com.example.demo.util.setImageBlob
import com.example.demo.data.Comment
import com.example.demo.util.PostAdapter
import com.example.demo.util.errorDialog
import com.example.demo.util.formatTimestamp
import com.example.demo.util.showConfirmationDialog
import com.google.firebase.Timestamp

class CommentFragment : Fragment() {

    private lateinit var binding: FragmentCommentBinding
    private val nav by lazy { findNavController() }
    private val postVM: PostVM by activityViewModels()
    private val commentVM: CommentVM by activityViewModels()
    private val postId by lazy { arguments?.getString("postId") ?: "" }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        val post =  postVM.get(postId)
        if (post == null || currentUserId == null) {
            nav.navigateUp()
            return null
        }
        binding.forumDesc.text = post.postDesc
        binding.postDateTime.text = post.postDate?.let { formatTimestamp(it)}.toString()
        binding.postImg.setImageBlob(post.img)
        fetchUserInfo(post.postOwnerId) { user ->
            binding.postOwner.text = user?.name ?: "Unknown User"
            user?.photo?.let { binding.ivProfile.setImageBlob(it) }
        }
        val adapter = CommentAdapter { h, c ->
            h.binding.btnDeleteComment.setOnClickListener {
                showConfirmationDialog("Delete Comment", "Are you sure you want to delete this comment?") {
                    commentVM.delete(c.id)
                }
            }
        }

        binding.commentRecycler.adapter = adapter
        binding.commentRecycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        commentVM.getResultLD().observe(viewLifecycleOwner) { comments ->
            val filteredComments = comments
                .filter { it.postId == postId }
                .sortedByDescending { it.commentDate }
            adapter.submitList(filteredComments)
            binding.txtComments.text = "Comments (" + filteredComments.size + ")"
        }

        // Set the click listener for the send button
        binding.btnSend.setOnClickListener {
            val commentText = binding.iptComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val comment = Comment(
                    postId = postId,
                    commentDesc = commentText,
                    commentOwnerId = currentUserId,
                    commentDate = Timestamp.now()
                )
                commentVM.add(comment)
                binding.iptComment.text.clear()
                binding.commentRecycler.postDelayed({
                    binding.commentRecycler.scrollToPosition(0)
                }, 100)
            }
        }

        binding.showMode.setOnClickListener {
            val showModeView = binding.showMode
            when(showModeView.text.toString()) {
                "Show More" -> {
                    showModeView.text = "Show Less"
                    binding.forumDesc.isSingleLine = false
                    binding.postImg.visibility = View.VISIBLE
                }
                "Show Less" -> {
                    showModeView.text = "Show More"
                    binding.forumDesc.isSingleLine = true
                    binding.postImg.visibility = View.GONE
                }
            }
            binding.commentRecycler.scrollToPosition(0)
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