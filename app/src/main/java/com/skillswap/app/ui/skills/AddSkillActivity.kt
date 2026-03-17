package com.skillswap.app.ui.skills

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.R
import com.skillswap.app.databinding.ActivityAddSkillBinding
import com.skillswap.app.ui.viewmodel.SkillState
import com.skillswap.app.ui.viewmodel.SkillViewModel
import com.skillswap.app.utils.SkillCategory
import com.skillswap.app.utils.SkillLevel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Activity for adding a new skill to the user's profile. */
class AddSkillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSkillBinding
    private lateinit var viewModel: SkillViewModel

    private var selectedCategory: SkillCategory = SkillCategory.OTHER
    private var selectedLevel: SkillLevel = SkillLevel.BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddSkillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = getString(R.string.title_add_skill)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel = ViewModelProvider(this)[SkillViewModel::class.java]

        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        // Category dropdown
        val categories = SkillCategory.entries.map { it.displayName }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(categoryAdapter)
        binding.actvCategory.setText(SkillCategory.OTHER.displayName, false)
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = SkillCategory.entries[position]
        }

        // Level dropdown
        val levels = SkillLevel.entries.map { it.displayName }
        val levelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, levels)
        binding.actvLevel.setAdapter(levelAdapter)
        binding.actvLevel.setText(SkillLevel.BEGINNER.displayName, false)
        binding.actvLevel.setOnItemClickListener { _, _, position, _ ->
            selectedLevel = SkillLevel.entries[position]
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val experience = binding.etExperience.text.toString().toIntOrNull() ?: 0
            val hours = binding.etHours.text.toString().toIntOrNull() ?: 1
            viewModel.addSkill(
                name = binding.etSkillName.text.toString(),
                category = selectedCategory,
                level = selectedLevel,
                description = binding.etDescription.text.toString(),
                experienceYears = experience,
                hoursPerWeek = hours
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.btnSave.isEnabled = !loading
            if (loading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.nameError.observe(this) { error ->
            binding.tilSkillName.error = error
        }

        viewModel.descError.observe(this) { error ->
            binding.tilDescription.error = error
        }

        viewModel.skillState.observe(this) { state ->
            when (state) {
                is SkillState.Added -> {
                    showToast(getString(R.string.msg_skill_added))
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
