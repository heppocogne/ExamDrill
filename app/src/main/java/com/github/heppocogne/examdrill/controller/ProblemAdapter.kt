package com.github.heppocogne.examdrill.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.heppocogne.examdrill.databinding.ItemProblemBinding
import com.github.heppocogne.examdrill.entity.ProblemEntity

class ProblemAdapter(
    private val onClick: (ProblemEntity) -> Unit,
) : ListAdapter<ProblemEntity, ProblemAdapter.ProblemViewHolder>(ProblemDiffCallback) {

    var categoryMap: Map<Int, String> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val binding = ItemProblemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProblemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val problem = getItem(position)
        holder.binding.textCategory.text = categoryMap[problem.categoryId] ?: ""
        holder.binding.textProblem.text = problem.text
        holder.itemView.setOnClickListener { onClick(problem) }
    }

    class ProblemViewHolder(val binding: ItemProblemBinding) : RecyclerView.ViewHolder(binding.root)

    private object ProblemDiffCallback : DiffUtil.ItemCallback<ProblemEntity>() {
        override fun areItemsTheSame(oldItem: ProblemEntity, newItem: ProblemEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProblemEntity, newItem: ProblemEntity) =
            oldItem == newItem
    }
}
