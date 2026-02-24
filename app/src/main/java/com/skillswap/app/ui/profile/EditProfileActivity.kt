package com.skillswap.app.ui.profile

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.databinding.ActivityEditProfileBinding
import com.skillswap.app.ui.viewmodel.ProfileState
import com.skillswap.app.ui.viewmodel.ProfileViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Edit Profile Activity — allows users to update their name, bio, campus, and profile picture. */
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel

    private val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    viewModel.uploadProfilePicture(it)
                    Glide.with(this).load(it).circleCrop().into(binding.ivProfilePicture)
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupListeners()
        observeState()

        // Load current profile to pre-fill fields
        viewModel.loadCurrentUserProfile()
    }

    private fun setupListeners() {
        binding.btnChangePhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val bio = binding.etBio.text.toString()
            val campus = binding.etCampus.text.toString()
            viewModel.updateProfile(name, bio, campus)
        }
    }

    private fun observeState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.nameError.observe(this) { error -> binding.tilName.error = error }

        viewModel.bioError.observe(this) { error -> binding.tilBio.error = error }

        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.etName.setText(it.name)
                binding.etBio.setText(it.bio)
                binding.etCampus.setText(it.campus)

                if (it.profilePictureUrl.isNotBlank()) {
                    Glide.with(this)
                            .load(it.profilePictureUrl)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .circleCrop()
                            .into(binding.ivProfilePicture)
                }
            }
        }

        viewModel.profileState.observe(this) { state ->
            when (state) {
                is ProfileState.Updated -> {
                    showToast("Profile updated!")
                    finish()
                }
                is ProfileState.PictureUploaded -> {
                    showToast("Photo uploaded!")
                }
                is ProfileState.Error -> {
                    showToast(state.message)
                }
                else -> {
                    /* Idle/Loaded — no action */
                }
            }
        }
    }
}
