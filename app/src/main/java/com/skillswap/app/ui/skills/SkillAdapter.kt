package com.skillswap.app.ui.skills

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skillswap.app.data.model.Skill
import com.skillswap.app.databinding.ItemSkillBinding

/**
 * Adapter for the user's own skills list on the Profile screen.
 * Uses [ListAdapter] + [DiffUtil] for efficient updates.
 */
class SkillAdapter(
    private val onEditClick: (Skill) -> Unit,
    private val onDeleteClick: (Skill) -> Unit
) : ListAdapter<Skill, SkillAdapter.SkillViewHolder>(SkillDiffCallback()) {

    inner class SkillViewHolder(private val binding: ItemSkillBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: Skill) {
            binding.tvSkillName.text = skill.name
            binding.tvCategory.text = skill.getSkillCategory().displayName
            binding.tvLevel.text = skill.getSkillLevel().displayName
            binding.tvDescription.text = skill.description
            binding.chipAvailable.text = if (skill.isAvailable) "Available" else "Unavailable"
            binding.chipAvailable.isSelected = skill.isAvailable

            binding.btnEdit.setOnClickListener { onEditClick(skill) }
            binding.btnDelete.setOnClickListener { onDeleteClick(skill) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemSkillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SkillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class SkillDiffCallback : DiffUtil.ItemCallback<Skill>() {
    override fun areItemsTheSame(oldItem: Skill, newItem: Skill) = oldItem.skillId == newItem.skillId
    override fun areContentsTheSame(oldItem: Skill, newItem: Skill) = oldItem == newItem
}
