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
import com.example.healthyme.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is already logged in
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            findNavController().navigate(R.id.action_signInFragment_to_navigation_home)
            return
        }

        // Set up click listeners
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate inputs
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

            // For demo: Check if user exists in SharedPreferences
            val savedEmail = sharedPreferences.getString("user_email", "")
            val savedPassword = sharedPreferences.getString("user_password", "")

            if (email == savedEmail && password == savedPassword) {
                // Login successful
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                findNavController().navigate(R.id.action_signInFragment_to_navigation_home)
            } else {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up navigation to Sign Up
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        // Set up navigation to Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
