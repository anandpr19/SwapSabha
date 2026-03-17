package com.skillswap.app.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skillswap.app.R
import com.skillswap.app.data.model.Skill
import com.skillswap.app.databinding.ItemSkillCardBinding

/**
 * Adapter for the Discovery feed — each card shows a skill with its owner's info.
 * Tapping a card opens that user's [StudentProfileActivity].
 */
class SkillCardAdapter(
    private val onCardClick: (Skill) -> Unit
) : ListAdapter<Skill, SkillCardAdapter.SkillCardViewHolder>(SkillCardDiff()) {

    inner class SkillCardViewHolder(private val binding: ItemSkillCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: Skill) {
            binding.tvSkillName.text = skill.name
            binding.tvUserName.text = skill.userName
            binding.tvCategory.text = skill.getSkillCategory().displayName
            binding.tvLevel.text = skill.getSkillLevel().displayName

            Glide.with(binding.root.context)
                .load(skill.userProfilePic.ifBlank { null })
                .placeholder(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(binding.ivUserAvatar)

            binding.root.setOnClickListener { onCardClick(skill) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillCardViewHolder {
        val binding =
                ItemSkillCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SkillCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class SkillCardDiff : DiffUtil.ItemCallback<Skill>() {
    override fun areItemsTheSame(oldItem: Skill, newItem: Skill) = oldItem.skillId == newItem.skillId
    override fun areContentsTheSame(oldItem: Skill, newItem: Skill) = oldItem == newItem
}
