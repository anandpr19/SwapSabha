package com.skillswap.app.ui.swaps

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skillswap.app.R
import com.skillswap.app.data.model.Swap
import com.skillswap.app.databinding.ActivitySwapDetailBinding
import com.skillswap.app.ui.viewmodel.SwapState
import com.skillswap.app.ui.viewmodel.SwapViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays swap details and provides action buttons based on the status and role.
 */
class SwapDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SWAP = "extra_swap"
    }

    private lateinit var binding: ActivitySwapDetailBinding
    private lateinit var viewModel: SwapViewModel
    private lateinit var swap: Swap

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySwapDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = getString(R.string.title_swap_detail)
            setDisplayHomeAsUpEnabled(true)
        }

        @Suppress("DEPRECATION")
        swap = intent.getSerializableExtra(EXTRA_SWAP) as? Swap
            ?: run { finish(); return }

        viewModel = ViewModelProvider(this)[SwapViewModel::class.java]

        populateUI()
        setupActions()
        observeViewModel()
    }

    private fun populateUI() {
        binding.tvSkillName.text = swap.skillName
        binding.chipStatus.text = swap.getSwapStatus().name
        binding.tvRequesterName.text = swap.requesterName
        binding.tvTeacherName.text = swap.teacherName
        binding.tvDuration.text = "${swap.duration} min"
        binding.tvLocation.text = swap.location.ifBlank { "Not specified" }

        binding.tvDate.text = if (swap.proposedDate > 0) {
            dateFormat.format(Date(swap.proposedDate))
        } else {
            "Not set"
        }

        if (swap.message.isNotBlank()) {
            binding.tvMessageLabel.show()
            binding.tvMessage.show()
            binding.tvMessage.text = swap.message
        }
    }

    private fun setupActions() {
        val userId = viewModel.getCurrentUserId() ?: return

        // Teacher can accept or reject pending requests
        if (swap.canRespondTo(userId)) {
            binding.btnAccept.show()
            binding.btnReject.show()

            binding.btnAccept.setOnClickListener {
                viewModel.acceptRequest(swap.swapId)
            }

            binding.btnReject.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.title_reject_swap)
                    .setMessage(R.string.msg_reject_confirm)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_reject) { _, _ ->
                        viewModel.rejectRequest(swap.swapId)
                    }
                    .show()
            }
        }

        // Both can complete accepted swaps
        if (swap.canComplete(userId)) {
            binding.btnComplete.show()
            binding.btnComplete.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.title_complete_swap)
                    .setMessage(R.string.msg_complete_confirm)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_complete) { _, _ ->
                        viewModel.completeSwap(swap.swapId)
                    }
                    .show()
            }
        }

        // Both can cancel pending/accepted swaps
        if (swap.canCancel(userId)) {
            binding.btnCancel.show()
            binding.btnCancel.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.title_cancel_swap)
                    .setMessage(R.string.msg_cancel_confirm)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_cancel_swap) { _, _ ->
                        viewModel.cancelSwap(swap.swapId)
                    }
                    .show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            if (loading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.swapState.observe(this) { state ->
            when (state) {
                is SwapState.Accepted -> {
                    showToast(getString(R.string.msg_swap_accepted))
                    setResult(RESULT_OK)
                    finish()
                }
                is SwapState.Rejected -> {
                    showToast(getString(R.string.msg_swap_rejected))
                    setResult(RESULT_OK)
                    finish()
                }
                is SwapState.Completed -> {
                    showToast(getString(R.string.msg_swap_completed))
                    setResult(RESULT_OK)
                    finish()
                }
                is SwapState.Cancelled -> {
                    showToast(getString(R.string.msg_swap_cancelled))
                    setResult(RESULT_OK)
                    finish()
                }
                is SwapState.Error -> showToast(state.message)
                else -> {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
