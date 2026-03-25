package com.skillswap.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.databinding.ItemLeaderboardBinding
import com.skillswap.app.utils.ReputationTier

/**
 * RecyclerView adapter for the leaderboard — shows top users by reputation.
 */
class LeaderboardAdapter(
    private var users: List<Map<String, Any>> = emptyList()
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val b = holder.binding

        b.tvRank.text = "#${position + 1}"
        b.tvName.text = user["name"] as? String ?: "—"

        val avgRating = (user["avgRating"] as? Double) ?: 0.0
        b.tvRating.text = "★ ${"%.1f".format(avgRating)}"

        val repScore = (user["reputationScore"] as? Int) ?: 0
        val tier = ReputationTier.fromScore(repScore)
        b.chipTier.text = tier.displayName

        val picUrl = user["profilePictureUrl"] as? String ?: ""
        Glide.with(b.ivAvatar.context)
            .load(picUrl.ifBlank { null })
            .placeholder(R.drawable.ic_launcher_foreground)
            .circleCrop()
            .into(b.ivAvatar)
    }

    override fun getItemCount() = users.size

    fun submitList(newUsers: List<Map<String, Any>>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
