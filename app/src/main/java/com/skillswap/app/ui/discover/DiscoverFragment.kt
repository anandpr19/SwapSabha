package com.skillswap.app.ui.discover

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skillswap.app.databinding.FragmentDiscoverBinding
import com.skillswap.app.ui.viewmodel.DiscoveryViewModel
import com.skillswap.app.utils.SkillCategory
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Discovery fragment — browse, search, and filter available skills from all users. */
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiscoveryViewModel
    private lateinit var adapter: SkillCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DiscoveryViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        observeViewModel()

        viewModel.loadSkills()
    }

    private fun setupRecyclerView() {
        adapter = SkillCardAdapter { skill ->
            val intent = Intent(requireContext(), StudentProfileActivity::class.java)
            intent.putExtra(StudentProfileActivity.EXTRA_USER_ID, skill.userId)
            startActivity(intent)
        }
        binding.rvSkills.adapter = adapter
        binding.rvSkills.layoutManager = LinearLayoutManager(requireContext())

        // Infinite scroll — load next page when near bottom
        binding.rvSkills.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                if (dy > 0 && lastVisible >= total - 3) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.searchSkills(query)

                // Toggle between search results and browse list
                if (query.isBlank()) {
                    binding.rvSkills.adapter = adapter
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupCategoryChips() {
        val chipToCategory = mapOf(
            binding.chipAll to null,
            binding.chipMusic to SkillCategory.MUSIC,
            binding.chipTech to SkillCategory.TECH,
            binding.chipSports to SkillCategory.SPORTS,
            binding.chipLanguages to SkillCategory.LANGUAGES,
            binding.chipArts to SkillCategory.ARTS,
            binding.chipOther to SkillCategory.OTHER
        )

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = binding.root.findViewById<com.google.android.material.chip.Chip>(checkedId)
            val category = chipToCategory[chip]
            viewModel.filterByCategory(category)
        }
    }

    private fun observeViewModel() {
        viewModel.discoverySkills.observe(viewLifecycleOwner) { skills ->
            adapter.submitList(skills)
            if (skills.isEmpty()) {
                binding.layoutEmpty.show()
                binding.rvSkills.hide()
            } else {
                binding.layoutEmpty.hide()
                binding.rvSkills.show()
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (binding.etSearch.text?.isNotBlank() == true) {
                adapter.submitList(results)
                if (results.isEmpty()) {
                    binding.layoutEmpty.show()
                    binding.rvSkills.hide()
                } else {
                    binding.layoutEmpty.hide()
                    binding.rvSkills.show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (adapter.currentList.isEmpty()) {
                if (loading) binding.progressBar.show() else binding.progressBar.hide()
            }
        }

        viewModel.discoveryError.observe(viewLifecycleOwner) { error ->
            error?.let {
                requireContext().showToast(it)
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
