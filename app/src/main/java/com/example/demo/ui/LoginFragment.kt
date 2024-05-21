package com.example.demo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.data.AuthVM
import com.example.demo.databinding.FragmentLoginBinding
import com.example.demo.util.errorDialog
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        // -----------------------------------------------------------------------------------------

        reset()
        binding.btnReset.setOnClickListener { reset() }
        binding.btnLogin.setOnClickListener { login() }
        binding.btnBack.setOnClickListener { nav.navigateUp() }

        // -----------------------------------------------------------------------------------------
        return binding.root
    }

    private fun reset() {
        binding.edtEmail.text.clear()
        binding.edtPassword.text.clear()
        binding.chkRemember.isChecked = false

        binding.edtEmail.requestFocus()
    }

    private fun login() {
        val email    = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val remember = binding.chkRemember.isChecked

        lifecycleScope.launch {
            val success = auth.login(email, password, remember)
            if (success) {
                nav.popBackStack(R.id.homeFragment, false)
                nav.navigateUp()
            }
            else {
                errorDialog("Invalid login credentials.")
            }
        }
    }
}








