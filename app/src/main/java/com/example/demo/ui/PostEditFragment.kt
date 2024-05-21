package com.example.demo.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.Post
import com.example.demo.data.PostVM
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.databinding.FragmentPostEditBinding
import com.example.demo.util.cropToBlob
import com.example.demo.util.errorDialog
import com.example.demo.util.formatTimestamp
import com.example.demo.util.setImageBlob
import com.example.demo.util.showConfirmationDialog
import com.example.demo.util.successDialog
import com.google.firebase.Timestamp

class PostEditFragment : Fragment() {

    private lateinit var binding: FragmentPostEditBinding
    private val nav by lazy { findNavController() }
    private val postVM: PostVM by activityViewModels()
    private val postId by lazy { arguments?.getString("postId") ?: "" }
    private val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostEditBinding.inflate(inflater, container, false)
        val post =  postVM.get(postId)
        if (post == null) {
            nav.navigateUp()
            return null
        }
        binding.edtPostDesc.setText(post.postDesc)
        binding.postDateTime.text = post.postDate?.let { formatTimestamp(it)}.toString()
        if(post.img != null && post.img.toBytes().isNotEmpty()) {
            binding.btnRemove.visibility = View.VISIBLE
            binding.postImg.setImageBlob(post.img)
        }

        fetchUserInfo(post.postOwnerId) { user ->
            binding.postOwner.text = user?.name ?: "Unknown User"
            user?.photo?.let { binding.ivProfile.setImageBlob(it) }
        }

        binding.btnRemove.setOnClickListener {
            binding.postImg.visibility = View.GONE
            binding.btnRemove.visibility = View.GONE
            binding.postImg.setImageURI(null)
        }

        binding.btnCancel.setOnClickListener {
            nav.navigateUp()
        }

        binding.btnUploadPhoto.setOnClickListener {
            select()
        }

        binding.btnSubmit.setOnClickListener {
            submit()
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        return binding.root
    }

    private fun submit() {
        val sharedPref = requireActivity().getSharedPreferences("AUTH",     Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        if (currentUserId == null) {
            errorDialog("User not found. Please login again.")
            nav.navigate(R.id.loginFragment)
            return
        }

        val description = binding.edtPostDesc.text.toString().trim()
        if (description.isEmpty()) {
            errorDialog("Description cannot be empty")
        } else {
            val p = Post (
                postOwnerId = currentUserId,
                postDesc = description,
                postDate = Timestamp.now() ,
                img    = binding.postImg.cropToBlob(400, 400)
            )
            showConfirmationDialog("Edit Post", "Are you sure you want to Edit this post?") {
                postVM.set(postId, p)
                successDialog("Post Edited Successfully")
                nav.navigateUp()
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val imageUri = it
        if (imageUri != null) {
            binding.postImg.setImageURI(imageUri)
            binding.btnRemove.visibility = View.VISIBLE
        } else {
            binding.btnRemove.visibility = View.GONE
        }
    }

    private fun select() {
        getContent.launch("image/*")
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                binding.postImg.setImageBitmap(it)
                binding.btnRemove.visibility = View.VISIBLE
            }
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Permission denied, handle accordingly
            }
        }
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