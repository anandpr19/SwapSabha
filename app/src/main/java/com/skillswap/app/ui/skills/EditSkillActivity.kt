package com.skillswap.app.ui.skills

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skillswap.app.R
import com.skillswap.app.data.model.Skill
import com.skillswap.app.databinding.ActivityEditSkillBinding
import com.skillswap.app.ui.viewmodel.SkillState
import com.skillswap.app.ui.viewmodel.SkillViewModel
import com.skillswap.app.utils.SkillCategory
import com.skillswap.app.utils.SkillLevel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Activity for editing or deleting an existing skill. */
class EditSkillActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SKILL = "extra_skill"
    }

    private lateinit var binding: ActivityEditSkillBinding
    private lateinit var viewModel: SkillViewModel
    private lateinit var skill: Skill

    private var selectedCategory: SkillCategory = SkillCategory.OTHER
    private var selectedLevel: SkillLevel = SkillLevel.BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditSkillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = getString(R.string.title_edit_skill)
            setDisplayHomeAsUpEnabled(true)
        }

        @Suppress("DEPRECATION")
        skill = intent.getSerializableExtra(EXTRA_SKILL) as? Skill
            ?: run { finish(); return }

        viewModel = ViewModelProvider(this)[SkillViewModel::class.java]

        setupDropdowns()
        prefillFields()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val categories = SkillCategory.entries.map { it.displayName }
        binding.actvCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        )
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = SkillCategory.entries[position]
        }

        val levels = SkillLevel.entries.map { it.displayName }
        binding.actvLevel.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, levels)
        )
        binding.actvLevel.setOnItemClickListener { _, _, position, _ ->
            selectedLevel = SkillLevel.entries[position]
        }
    }

    private fun prefillFields() {
        selectedCategory = skill.getSkillCategory()
        selectedLevel = skill.getSkillLevel()

        binding.etSkillName.setText(skill.name)
        binding.actvCategory.setText(selectedCategory.displayName, false)
        binding.actvLevel.setText(selectedLevel.displayName, false)
        binding.etDescription.setText(skill.description)
        binding.etExperience.setText(skill.experienceYears.toString())
        binding.etHours.setText(skill.hoursPerWeek.toString())
        binding.switchAvailable.isChecked = skill.isAvailable
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val experience = binding.etExperience.text.toString().toIntOrNull() ?: 0
            val hours = binding.etHours.text.toString().toIntOrNull() ?: 1
            viewModel.updateSkill(
                skillId = skill.skillId,
                name = binding.etSkillName.text.toString(),
                category = selectedCategory,
                level = selectedLevel,
                description = binding.etDescription.text.toString(),
                experienceYears = experience,
                hoursPerWeek = hours,
                isAvailable = binding.switchAvailable.isChecked
            )
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_delete_skill)
                .setMessage(R.string.msg_delete_skill_confirm)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete) { _, _ ->
                    viewModel.deleteSkill(skill.skillId)
                }
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.btnSave.isEnabled = !loading
            binding.btnDelete.isEnabled = !loading
            if (loading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.nameError.observe(this) { error -> binding.tilSkillName.error = error }
        viewModel.descError.observe(this) { error -> binding.tilDescription.error = error }

        viewModel.skillState.observe(this) { state ->
            when (state) {
                is SkillState.Updated -> {
                    showToast(getString(R.string.msg_skill_updated))
                    setResult(RESULT_OK)
                    finish()
                }
                is SkillState.Deleted -> {
                    showToast(getString(R.string.msg_skill_deleted))
                    setResult(RESULT_OK)
                    finish()
                }
                is SkillState.Error -> showToast(state.message)
                else -> {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
