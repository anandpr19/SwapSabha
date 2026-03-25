package com.skillswap.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.skillswap.app.R
import com.skillswap.app.databinding.FragmentHomeBinding
import com.skillswap.app.ui.swaps.SwapAdapter
import com.skillswap.app.ui.viewmodel.HomeViewModel
import com.skillswap.app.utils.BadgeType
import com.skillswap.app.utils.ReputationTier
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import java.util.Calendar

/**
 * Home dashboard showing stats, badges, recent swaps, and leaderboard.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var recentSwapAdapter: SwapAdapter
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()
        setGreeting()

        viewModel.loadDashboard()
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
        binding.tvGreeting.text = greeting
        binding.tvUserName.text = viewModel.getUserName()
    }

    private fun setupRecyclerViews() {
        val currentUserId = viewModel.getCurrentUserId() ?: ""
        recentSwapAdapter = SwapAdapter(currentUserId) { /* tap on swap — could open detail */ }
        binding.rvRecentSwaps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentSwaps.adapter = recentSwapAdapter

        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = leaderboardAdapter
    }

    private fun observeViewModel() {
        // User stats
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.tvUserName.text = user.name
            binding.tvSwapCount.text = user.stats.completedSwaps.toString()

            val avgRating = user.stats.avgRating
            binding.tvAvgRating.text = if (avgRating > 0) "%.1f".format(avgRating) else "—"

            val tier = ReputationTier.fromScore(user.reputationScore)
            binding.tvRepTier.text = tier.displayName

            // Badges
            if (user.badges.isNotEmpty()) {
                binding.tvBadgesLabel.show()
                binding.chipGroupBadges.show()
                binding.chipGroupBadges.removeAllViews()
                user.badges.forEach { badgeName ->
                    val badge = runCatching { BadgeType.valueOf(badgeName) }.getOrNull()
                    badge?.let {
                        val chip = Chip(requireContext()).apply {
                            text = "${it.icon} ${it.displayName}"
                            isClickable = false
                        }
                        binding.chipGroupBadges.addView(chip)
                    }
                }
            }
        }

        // Recent swaps
        viewModel.recentSwaps.observe(viewLifecycleOwner) { swaps ->
            if (swaps.isEmpty()) {
                binding.tvNoRecentSwaps.show()
                binding.rvRecentSwaps.hide()
            } else {
                binding.tvNoRecentSwaps.hide()
                binding.rvRecentSwaps.show()
                recentSwapAdapter.submitList(swaps)
            }
        }

        // Leaderboard
        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            if (users.isEmpty()) {
                binding.tvNoLeaderboard.show()
                binding.rvLeaderboard.hide()
            } else {
                binding.tvNoLeaderboard.hide()
                binding.rvLeaderboard.show()
                leaderboardAdapter.submitList(users)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
