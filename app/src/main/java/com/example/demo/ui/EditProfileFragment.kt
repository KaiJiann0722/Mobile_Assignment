package com.example.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.AuthVM
import com.example.demo.data.User
import com.example.demo.data.UserVM
import com.example.demo.databinding.EditProfileBinding
import com.example.demo.util.cropToBlob
import com.example.demo.util.errorDialog
import com.example.demo.util.toast
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class EditProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private val fieldCollectionRef = db.collection("field")
    private lateinit var binding: EditProfileBinding
    private val vm: UserVM by activityViewModels()
    private val auth: AuthVM by activityViewModels()
    private val nav by lazy { findNavController() }

    private val fieldList = mutableListOf<String>()
    private val fieldMap = HashMap<String, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = EditProfileBinding.inflate(inflater, container, false)

        binding.btnSave.setOnClickListener { save() }
        binding.imageView2.setOnClickListener { select() }


        fetchFieldData()


        return binding.root
    }

    private fun fetchFieldData(){
        fieldCollectionRef.get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                for (document in querySnapshot.documents) {
                    val fieldName = document.getString("name") ?: ""
                    val fieldId = document.id
                    fieldMap[fieldName] = fieldId
                    fieldList.add(fieldName)
                }
                // Populate Spinner with fetched data
                binding.spnEditField.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fieldList)
            }
            .addOnFailureListener { e: Exception ->
                // Handle error while fetching data
                errorDialog("Failed to fetch data: ${e.message}")
            }
    }

    private fun save() {
            val Email           = binding.edtEmail.text.toString().trim()
            val Password        = binding.edtChangePassword.text.toString().trim()
            val ConfirmPassword = binding.edtConfirmChangePassword.text.toString().trim()
            val Name            = binding.edtProfileName.text.toString().trim()
            val DOB             = binding.edtDateOfBirth.text.toString().trim()
            val Field           = binding.spnEditField.selectedItem.toString()
            val Gender          = if (binding.rbnMale.isChecked) "Male" else "Female"
            val Photo           = binding.imageView2.cropToBlob(300, 300)
            val Bio             = binding.edtBio.text.toString()

            if (Password != ConfirmPassword) {
                toast("Passwords do not match")
                return
            }
            // Fetch the field ID from fieldMap using the selected field name
            val fieldID = fieldMap[Field] ?: ""

            // Insert user
            val user = auth.getUserLD().value?.copy(
                email = Email,
                dateOfBirth = DOB,
                fieldID = fieldID,
                gender = Gender,
                password = Password,
                name = Name,
                photo = Photo,
                bio = Bio
            )

        if (user != null) {
            vm.set(user)
        }
        nav.navigateUp()


    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.imageView2.setImageURI(it)
    }

    private fun select() {
        // Select file
        getContent.launch("image/*")
    }


}
