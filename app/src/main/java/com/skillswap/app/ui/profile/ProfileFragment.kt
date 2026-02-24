package com.skillswap.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.databinding.FragmentProfileBinding
import com.skillswap.app.ui.auth.LoginActivity
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.ui.viewmodel.ProfileState
import com.skillswap.app.ui.viewmodel.ProfileViewModel
import com.skillswap.app.utils.DateFormatter
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Profile Fragment — displays the current user's profile. */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthViewModel

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

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeState()

        // Load the current user's profile
        profileViewModel.loadCurrentUserProfile()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    private fun observeState() {
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        profileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.name.ifBlank { "User" }
                binding.tvCampus.text = it.campus.ifBlank { "No campus set" }
                binding.tvBio.text = it.bio.ifBlank { "No bio yet" }
                binding.tvSwapsCount.text = it.totalSwaps.toString()
                binding.tvHoursCount.text = String.format("%.0f", it.totalHours)
                binding.tvRating.text =
                        if (it.stats.ratingCount > 0) {
                            String.format("%.1f", it.stats.avgRating)
                        } else "—"
                binding.tvJoinedDate.text = "Joined ${DateFormatter.formatDate(it.joinDate)}"

                // Load profile picture with Glide
                if (it.profilePictureUrl.isNotBlank()) {
                    Glide.with(this)
                            .load(it.profilePictureUrl)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .circleCrop()
                            .into(binding.ivProfilePicture)
                }
            }
        }

        profileViewModel.profileState.observe(viewLifecycleOwner) { state ->
            if (state is ProfileState.Error) {
                requireContext().showToast(state.message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit screen
        profileViewModel.loadCurrentUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
