package com.github.heppocogne.examdrill.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.heppocogne.examdrill.databinding.ItemExamBinding
import com.github.heppocogne.examdrill.entity.ExamEntity

class ExamAdapter(
    private val onClick: (ExamEntity) -> Unit,
) : ListAdapter<ExamEntity, ExamAdapter.ExamViewHolder>(ExamDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding = ItemExamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = getItem(position)
        holder.binding.btnExam.text = exam.name
        holder.binding.btnExam.setOnClickListener { onClick(exam) }
    }

    class ExamViewHolder(val binding: ItemExamBinding) : RecyclerView.ViewHolder(binding.root)

    private object ExamDiffCallback : DiffUtil.ItemCallback<ExamEntity>() {
        override fun areItemsTheSame(oldItem: ExamEntity, newItem: ExamEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ExamEntity, newItem: ExamEntity) =
            oldItem == newItem
    }
}
