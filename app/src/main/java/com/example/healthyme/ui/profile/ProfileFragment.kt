package com.example.healthyme.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.healthyme.R
import com.example.healthyme.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if user is logged in
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            findNavController().navigate(R.id.action_profileFragment_to_signInFragment)
            return
        }

        // Load user data
        val userName = sharedPreferences.getString("user_name", "") ?: ""
        val userEmail = sharedPreferences.getString("user_email", "") ?: ""
        
        // Set user data
        binding.tvWelcome.text = "Hi, $userName 👋"
        binding.tvName.text = userName
        binding.tvEmail.text = userEmail

        // Load profile picture
        val profilePicUri = sharedPreferences.getString("profile_picture_uri", "")
        loadProfilePicture(profilePicUri)

        // Edit picture button click
        binding.btnEditPicture.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Sign out button click
        binding.btnSignOut.setOnClickListener {
            handleSignOut()
        }
    }

    private fun loadProfilePicture(profilePicUri: String?) {
        Glide.with(this)
            .load(profilePicUri.takeIf { !it.isNullOrEmpty() } ?: R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.ivProfile)
    }

    private fun handleSelectedImage(uri: Uri) {
        // Save the URI to SharedPreferences
        val editor = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE).edit()
        editor.putString("profile_picture_uri", uri.toString())
        editor.apply()
        
        // Load the new image
        loadProfilePicture(uri.toString())
        
        // Show success message
        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
    }

    private fun handleSignOut() {
        // Clear all user data
        val editor = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
        
        // Show success message
        Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate to sign in screen
        findNavController().navigate(R.id.action_profileFragment_to_signInFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}