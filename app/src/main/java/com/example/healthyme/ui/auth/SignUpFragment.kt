package com.example.healthyme.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.healthyme.R
import com.example.healthyme.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Validate inputs
            if (username.isEmpty()) {
                binding.usernameLayout.error = "Username is required"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLayout.error = "Please enter a valid email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.passwordLayout.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordLayout.error = "Please confirm your password"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.confirmPasswordLayout.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Save user data to SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("user_name", username)
            editor.putString("user_email", email)
            editor.putString("user_password", password)
            editor.apply()

            // Show success message
            Toast.makeText(requireContext(), "Account created successfully! Please log in.", Toast.LENGTH_LONG).show()

            // Navigate back to sign in
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
