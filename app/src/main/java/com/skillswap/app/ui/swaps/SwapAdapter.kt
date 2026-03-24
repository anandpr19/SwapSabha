package com.skillswap.app.ui.swaps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skillswap.app.data.model.Swap
import com.skillswap.app.databinding.ItemSwapBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying swap items in RecyclerViews.
 * Shows skill name, other user, status, date, and duration.
 */
class SwapAdapter(
    private val currentUserId: String,
    private val onSwapClick: (Swap) -> Unit
) : ListAdapter<Swap, SwapAdapter.SwapViewHolder>(SwapDiff()) {

    inner class SwapViewHolder(private val binding: ItemSwapBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())

        fun bind(swap: Swap) {
            binding.tvSkillName.text = swap.skillName
            binding.chipStatus.text = swap.getSwapStatus().name
            binding.tvDuration.text = "${swap.duration} min"

            // Show the other participant's name
            val otherName = if (swap.requesterId == currentUserId) {
                "with ${swap.teacherName}"
            } else {
                "from ${swap.requesterName}"
            }
            binding.tvOtherUser.text = otherName

            // Format date
            binding.tvDate.text = if (swap.proposedDate > 0) {
                dateFormat.format(Date(swap.proposedDate))
            } else {
                "Date not set"
            }

            binding.root.setOnClickListener { onSwapClick(swap) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapViewHolder {
        val binding = ItemSwapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SwapViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwapViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class SwapDiff : DiffUtil.ItemCallback<Swap>() {
    override fun areItemsTheSame(oldItem: Swap, newItem: Swap) = oldItem.swapId == newItem.swapId
    override fun areContentsTheSame(oldItem: Swap, newItem: Swap) = oldItem == newItem
}
