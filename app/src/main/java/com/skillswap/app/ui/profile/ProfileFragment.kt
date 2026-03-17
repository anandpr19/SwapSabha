package com.skillswap.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.data.model.Skill
import com.skillswap.app.databinding.FragmentProfileBinding
import com.skillswap.app.ui.auth.LoginActivity
import com.skillswap.app.ui.skills.AddSkillActivity
import com.skillswap.app.ui.skills.EditSkillActivity
import com.skillswap.app.ui.skills.SkillAdapter
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.ui.viewmodel.ProfileState
import com.skillswap.app.ui.viewmodel.ProfileViewModel
import com.skillswap.app.ui.viewmodel.SkillViewModel
import com.skillswap.app.utils.DateFormatter
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Profile Fragment — displays the current user's profile and their skills. */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var skillViewModel: SkillViewModel
    private lateinit var skillAdapter: SkillAdapter

    /** Launcher that reloads skills when AddSkillActivity returns RESULT_OK. */
    private val addSkillLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    skillViewModel.loadMySkills()
                }
            }

    /** Launcher that reloads skills when EditSkillActivity returns RESULT_OK. */
    private val editSkillLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    skillViewModel.loadMySkills()
                }
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

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        skillViewModel = ViewModelProvider(this)[SkillViewModel::class.java]

        setupSkillsRecyclerView()
        setupListeners()
        observeState()

        profileViewModel.loadCurrentUserProfile()
        skillViewModel.loadMySkills()
    }

    private fun setupSkillsRecyclerView() {
        skillAdapter = SkillAdapter(
            onEditClick = { skill: Skill ->
                val intent = Intent(requireContext(), EditSkillActivity::class.java)
                intent.putExtra(EditSkillActivity.EXTRA_SKILL, skill as java.io.Serializable)
                editSkillLauncher.launch(intent)
            },
            onDeleteClick = { skill: Skill ->
                skillViewModel.deleteSkill(skill.skillId)
            }
        )
        binding.rvMySkills.adapter = skillAdapter
        binding.rvMySkills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMySkills.isNestedScrollingEnabled = false
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnAddSkill.setOnClickListener {
            addSkillLauncher.launch(Intent(requireContext(), AddSkillActivity::class.java))
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
            if (state is ProfileState.Error) requireContext().showToast(state.message)
        }

        // Skills
        skillViewModel.mySkills.observe(viewLifecycleOwner) { skills ->
            skillAdapter.submitList(skills)
            if (skills.isEmpty()) {
                binding.tvNoSkills.show()
                binding.rvMySkills.hide()
            } else {
                binding.tvNoSkills.hide()
                binding.rvMySkills.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.loadCurrentUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
