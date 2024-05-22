package com.example.demo.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.data.Post
import com.example.demo.data.PostVM
import com.example.demo.databinding.FragmentPostBinding
import com.example.demo.util.cropToBlob
import com.example.demo.util.errorDialog
import com.example.demo.util.successDialog
import com.google.firebase.Timestamp
import java.util.Locale
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demo.R
import com.example.demo.data.FriendsVM
import com.example.demo.data.USERS
import com.example.demo.data.User
import com.example.demo.util.formatTimestamp
import com.example.demo.util.setImageBlob

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private val nav by lazy { findNavController() }
    private val postVM: PostVM by activityViewModels()
    private val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        fetchUserInfo()
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

        binding.btnRemove.setOnClickListener {
            binding.btnRemove.visibility = View.GONE
            binding.postImg.setImageURI(null)
        }

        return binding.root

    }

    private fun submit() {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
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
                postOwnerName = binding.postOwner.text.toString(),
                postDesc = description,
                postDate = Timestamp.now() ,
                img    = binding.postImg.cropToBlob(300, 300)
            )
            postVM.add(p)
            successDialog("Post Added Successfully")
            nav.navigateUp()
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

    private fun fetchUserInfo() {
        val sharedPref = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("userId", null)
        if (currentUserId != null) {
            USERS.document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            binding.ivProfile.setImageBlob(user.photo)
                            binding.postOwner.text = user.name
                        }

                    }
                }
        } else {
            nav.navigateUp()
        }
    }

}