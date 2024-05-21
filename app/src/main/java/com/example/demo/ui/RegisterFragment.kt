package com.example.demo.ui

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.demo.data.User
import com.example.demo.data.UserVM
import com.example.demo.databinding.FragmentRegisterBinding
import com.example.demo.util.cropToBlob
import com.example.demo.util.errorDialog
import com.example.demo.util.toast
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class RegisterFragment : Fragment() {

    private val db = Firebase.firestore
    private val fieldCollectionRef = db.collection("field")
    private val fieldList = mutableListOf<String>()
    private val fieldMap = HashMap<String, String>()
    private lateinit var binding: FragmentRegisterBinding
    private val nav by lazy { findNavController() }
    private val vm: UserVM by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        // -----------------------------------------------------------------------------------------

        reset()
        binding.imgPhoto.setOnClickListener { select() }
        binding.btnReset.setOnClickListener { reset() }
        binding.btnRegister.setOnClickListener { register() }
        binding.btnBack.setOnClickListener { nav.navigateUp() }

        // Fetch data for Spinner (spnField) from Firestore
        fieldCollectionRef.get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                for (document in querySnapshot.documents) {
                    val fieldName = document.getString("name") ?: ""
                    val fieldId = document.id
                    // Populate Spinner with field names
                    // Populate Spinner with field names and store id in the fieldMap
                    fieldMap[fieldName] = fieldId
                    // Store the field name in the fieldList
                    fieldList.add(fieldName)

                }
                // Populate Spinner with fetched data
                binding.spnField.adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, fieldList)
            }
            .addOnFailureListener { e: Exception ->
                // Handle error while fetching data
                errorDialog("Failed to fetch data: ${e.message}")
            }

        // -----------------------------------------------------------------------------------------
        return binding.root
    }




    private fun reset() {
        binding.edtEmail.text.clear()
        binding.edtName.text.clear()
        binding.rdbMale.isChecked = true
        binding.edtPassword.text.clear()
        binding.edtConfirmPassword.text.clear()
        binding.edtDOB.text.clear()
        binding.spnField.setSelection(0)
        binding.imgPhoto.setImageDrawable(null)

        binding.edtEmail.requestFocus()
    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.imgPhoto.setImageURI(it)
    }

    private fun select() {
        // Select file
        getContent.launch("image/*")
    }

    private fun register() {
        val Email           = binding.edtEmail.text.toString().trim()
        val Password        = binding.edtPassword.text.toString().trim()
        val ConfirmPassword = binding.edtConfirmPassword.text.toString().trim()
        val Name            = binding.edtName.text.toString().trim()
        val DOB             = binding.edtDOB.text.toString().trim()
        val Field           = binding.spnField.selectedItem.toString()
        val Gender          = if (binding.rdbMale.isChecked) "Male" else "Female"
        val Photo           = binding.imgPhoto.cropToBlob(300, 300)

        // Fetch the last user ID from Firestore
        val lastUser = vm.getAll().maxByOrNull { it.id.removePrefix("U").toInt() }
        val lastIdNum = lastUser?.id?.removePrefix("U")?.toIntOrNull() ?: 0

        // Generate the next user ID
        val nextId: String = "U${lastIdNum + 1}"

        if (Password != ConfirmPassword) {
            toast("Passwords do not match")
            return
        }
        // Fetch the field ID from fieldMap using the selected field name
        val fieldID = fieldMap[Field] ?: ""

        // Insert user
        val user = User(
            id    = nextId,
            email = Email,
            dateOfBirth = DOB,
            fieldID = fieldID,
            gender = Gender,
            password = Password,
            name = Name,
            photo = Photo
        )

        val e = vm.validate(user)
        if (e != "") {
            errorDialog(e)
            return
        }

        vm.set(user)
        nav.navigateUp()
    }

}