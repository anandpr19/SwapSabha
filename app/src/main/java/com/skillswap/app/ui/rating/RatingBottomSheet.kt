package com.skillswap.app.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.skillswap.app.R
import com.skillswap.app.databinding.BottomSheetRatingBinding
import com.skillswap.app.ui.viewmodel.RatingState
import com.skillswap.app.ui.viewmodel.RatingViewModel
import com.skillswap.app.utils.PositiveTag
import com.skillswap.app.utils.RatingRole
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/**
 * BottomSheet shown after a swap is completed, allowing users to rate each other.
 */
class RatingBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_SWAP_ID = "swap_id"
        private const val ARG_RATED_USER_ID = "rated_user_id"
        private const val ARG_RATED_USER_NAME = "rated_user_name"
        private const val ARG_ROLE = "role"

        fun newInstance(
            swapId: String,
            ratedUserId: String,
            ratedUserName: String,
            role: RatingRole
        ): RatingBottomSheet {
            return RatingBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SWAP_ID, swapId)
                    putString(ARG_RATED_USER_ID, ratedUserId)
                    putString(ARG_RATED_USER_NAME, ratedUserName)
                    putString(ARG_ROLE, role.name)
                }
            }
        }
    }

    private var _binding: BottomSheetRatingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RatingViewModel
    private var onRatingSubmitted: (() -> Unit)? = null

    fun setOnRatingSubmittedListener(listener: () -> Unit) {
        onRatingSubmitted = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[RatingViewModel::class.java]

        val ratedUserName = arguments?.getString(ARG_RATED_USER_NAME) ?: "user"
        binding.tvRateUser.text = "How was your session with $ratedUserName?"

        setupTagChips()
        setupSubmitButton()
        observeViewModel()
    }

    private fun setupTagChips() {
        PositiveTag.entries.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag.displayName
                isCheckable = true
                isCheckedIconVisible = true
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmitRating.setOnClickListener {
            val swapId = arguments?.getString(ARG_SWAP_ID) ?: return@setOnClickListener
            val ratedUserId = arguments?.getString(ARG_RATED_USER_ID) ?: return@setOnClickListener
            val roleStr = arguments?.getString(ARG_ROLE) ?: RatingRole.LEARNER.name
            val role = runCatching { RatingRole.valueOf(roleStr) }.getOrDefault(RatingRole.LEARNER)
            val stars = binding.ratingBar.rating.toInt()
            val comment = binding.etComment.text?.toString() ?: ""

            // Collect selected tags
            val selectedTags = mutableListOf<String>()
            for (i in 0 until binding.chipGroupTags.childCount) {
                val chip = binding.chipGroupTags.getChildAt(i) as? Chip
                if (chip?.isChecked == true) {
                    selectedTags.add(chip.text.toString())
                }
            }

            viewModel.submitRating(swapId, ratedUserId, role, stars, comment, selectedTags)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.progressBar.show() else binding.progressBar.hide()
            binding.btnSubmitRating.isEnabled = !loading
        }

        viewModel.ratingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RatingState.Submitted -> {
                    requireContext().showToast(getString(R.string.msg_rating_submitted))
                    viewModel.clearState()
                    onRatingSubmitted?.invoke()
                    dismiss()
                }
                is RatingState.Error -> {
                    requireContext().showToast(state.message)
                    viewModel.clearState()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
