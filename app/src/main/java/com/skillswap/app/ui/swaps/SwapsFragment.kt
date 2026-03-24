package com.skillswap.app.ui.swaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.skillswap.app.databinding.FragmentSwapsBinding

/**
 * Top-level Swaps fragment with TabLayout + ViewPager2.
 * Three tabs: Incoming Requests, Active Swaps, History.
 */
class SwapsFragment : Fragment() {

    private var _binding: FragmentSwapsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabTitles = listOf("Incoming", "Active", "History")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> IncomingRequestsFragment()
                1 -> ActiveSwapsFragment()
                2 -> SwapHistoryFragment()
                else -> IncomingRequestsFragment()
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
