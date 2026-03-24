package com.skillswap.app.ui.swaps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillswap.app.databinding.FragmentSwapListBinding
import com.skillswap.app.ui.viewmodel.SwapViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show

/** Shows completed, rejected, and cancelled swaps (history). */
class SwapHistoryFragment : Fragment() {

    private var _binding: FragmentSwapListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SwapViewModel
    private lateinit var adapter: SwapAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwapListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SwapViewModel::class.java]

        val userId = viewModel.getCurrentUserId() ?: ""
        adapter = SwapAdapter(userId) { swap ->
            val intent = Intent(requireContext(), SwapDetailActivity::class.java)
            intent.putExtra(SwapDetailActivity.EXTRA_SWAP, swap as java.io.Serializable)
            startActivity(intent)
        }
        binding.rvSwaps.adapter = adapter
        binding.rvSwaps.layoutManager = LinearLayoutManager(requireContext())

        viewModel.swapHistory.observe(viewLifecycleOwner) { swaps ->
            adapter.submitList(swaps)
            if (swaps.isEmpty()) { binding.tvEmpty.show(); binding.rvSwaps.hide() }
            else { binding.tvEmpty.hide(); binding.rvSwaps.show() }
        }

        viewModel.loadHistory()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
