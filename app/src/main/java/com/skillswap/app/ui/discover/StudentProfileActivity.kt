package com.skillswap.app.ui.discover

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.data.model.Skill
import com.skillswap.app.databinding.ActivityStudentProfileBinding
import com.skillswap.app.ui.skills.SkillAdapter
import com.skillswap.app.ui.viewmodel.ProfileViewModel
import com.skillswap.app.ui.viewmodel.ProfileState
import com.skillswap.app.ui.viewmodel.SkillViewModel
import com.skillswap.app.ui.viewmodel.SkillState
import com.skillswap.app.utils.ReputationTier
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Shows another user's public profile — name, bio, skills, reputation. */
class StudentProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private lateinit var binding: ActivityStudentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var skillViewModel: SkillViewModel
    private lateinit var skillAdapter: SkillAdapter
    private var targetUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        targetUserId = intent.getStringExtra(EXTRA_USER_ID) ?: run { finish(); return }

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        skillViewModel = ViewModelProvider(this)[SkillViewModel::class.java]

        setupSkillsRecyclerView()
        observeViewModels()

        profileViewModel.loadProfile(targetUserId)
        loadTargetUserSkills()

        // Request Swap button — wired up in Phase 3
        binding.btnRequestSwap.setOnClickListener {
            showToast(getString(R.string.msg_swap_coming_soon))
        }
    }

    private fun setupSkillsRecyclerView() {
        // Read-only on student profile — no edit/delete actions
        skillAdapter = SkillAdapter(
            onEditClick = {},
            onDeleteClick = {}
        )
        binding.rvStudentSkills.adapter = skillAdapter
        binding.rvStudentSkills.layoutManager = LinearLayoutManager(this)
        binding.rvStudentSkills.isNestedScrollingEnabled = false
    }

    private fun loadTargetUserSkills() {
        // Temporarily override the ViewModel's userId by loading skills directly
        // via SkillRepository through a coroutine launched from the Activity scope.
        // SkillViewModel.loadMySkills() uses prefs — so we use ProfileViewModel path for now.
        // TODO: expose a loadSkillsForUser(userId) method in SkillViewModel for Phase 3.
        lifecycleScope.launchWhenStarted {
            val repo = com.skillswap.app.data.repository.SkillRepository()
            val result = repo.getMySkills(targetUserId)
            result.onSuccess { skills ->
                runOnUiThread {
                    if (skills.isEmpty()) {
                        binding.tvNoSkills.show()
                        binding.rvStudentSkills.hide()
                    } else {
                        binding.tvNoSkills.hide()
                        binding.rvStudentSkills.show()
                        skillAdapter.submitList(skills)
                    }
                }
            }
        }
    }

    private fun observeViewModels() {
        profileViewModel.userProfile.observe(this) { user ->
            user ?: return@observe
            supportActionBar?.title = user.name

            binding.tvName.text = user.name
            binding.tvCampus.text = user.campus.ifBlank { "Campus not set" }
            binding.tvBio.text = user.bio.ifBlank { "No bio yet." }

            val tier = ReputationTier.fromScore(user.reputationScore)
            binding.chipReputation.text =
                "${tier.displayName} · ${user.reputationScore} pts"

            Glide.with(this)
                .load(user.profilePictureUrl.ifBlank { null })
                .placeholder(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(binding.ivAvatar)
        }

        profileViewModel.profileState.observe(this) { state ->
            if (state is ProfileState.Error) showToast(state.message)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
