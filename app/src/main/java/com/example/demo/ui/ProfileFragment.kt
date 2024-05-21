package com.example.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.AuthVM
import com.example.demo.data.User
import com.example.demo.databinding.FragmentProfileBinding
import com.example.demo.util.errorDialog
import com.example.demo.util.setImageBlob
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private lateinit var binding: FragmentProfileBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private lateinit var firestore: FirebaseFirestore
    private val fieldMap = HashMap<String, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener { nav.navigateUp() }
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnEditProfile.setOnClickListener { showEdit()  }
        binding.btnDelete.setOnClickListener { delete() }

        // Fetch field data and then set the profile
        fetchFieldData {
            // Directly get the current user using getUser()
            val currentUser = auth.getUser()
            if (currentUser != null) {
                setProfile(currentUser)
            } else {
                displayGuestInfo()
            }

            // Observe login status to handle changes
            auth.getUserLD().observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    setProfile(user)
                } else {
                    displayGuestInfo()
                }
            }
        }

        return binding.root
    }

    private fun fetchFieldData(onComplete: () -> Unit) {
        firestore.collection("field").get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val fieldName = document.getString("name") ?: ""
                val fieldId = document.id
                fieldMap[fieldId] = fieldName
            }
            onComplete()
        }.addOnFailureListener { e ->
            // Handle the error
            errorDialog("Failed to fetch field data: ${e.message}")
            onComplete()
        }
    }

    private fun setProfile(user: User){
        binding.lblProfileName.text = user.name
        binding.txtBio.text = user.bio

        val fieldName = fieldMap[user.fieldID] ?: "Unknown Field"

        binding.txtPersonalInfo.text = """
            Name: ${user.name}
            Email: ${user.email}
            Date of Birth: ${user.dateOfBirth}
            Gender: ${user.gender}
            Field: $fieldName
        """.trimIndent()

        user.photo.let { blob ->
            binding.imageView.setImageBlob(blob)
        }

    }

    private fun showEdit() {
        nav.navigate(R.id.editProfileFragment)
    }

    private fun displayGuestInfo() {
        binding.lblProfileName.text = "Guest"
        binding.txtBio.text = ""
        binding.txtPersonalInfo.text = "Not Login Yet"
    }

    private fun logout() {
        auth.logout() // Assuming you have a logout method in your AuthVM
        nav.popBackStack(R.id.homeFragment, false)
        nav.navigateUp()
    }

    private fun delete(){
        val user = auth.getUser()

        // Check if the user is null
        if (user == null) {
            errorDialog("User is null. Cannot delete account.")
            return
        }

        // Get the user ID
        val userId = user.id

        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Firebase.auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Firebase.auth.signOut()
                        nav.navigate(R.id.homeFragment)
                    } else {
                        errorDialog("Failed to delete user authentication: ${task.exception?.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                errorDialog("Failed to delete user data: ${e.message}")
            }
    }
}
