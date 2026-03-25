package com.github.heppocogne.examdrill.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.controller.ExamDetailFragment
import com.github.heppocogne.examdrill.databinding.DialogAddExamBinding
import com.github.heppocogne.examdrill.databinding.FragmentHomeBinding
import com.github.heppocogne.examdrill.model.ExamModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var examModel: ExamModel
    private lateinit var examAdapter: ExamAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        examModel = ExamModel(requireContext().applicationContext)

        examAdapter = ExamAdapter { exam ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ExamDetailFragment.newInstance(exam.id, exam.name))
                .addToBackStack(null)
                .commit()
        }
        binding.examList.layoutManager = LinearLayoutManager(requireContext())
        binding.examList.adapter = examAdapter

        binding.fabAddExam.setOnClickListener { showAddExamDialog() }
        observeExams()
    }

    private fun observeExams() {
        viewLifecycleOwner.lifecycleScope.launch {
            examModel.getAllExams().collect { exams ->
                examAdapter.submitList(exams)
                binding.textEmpty.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
                binding.examList.visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showAddExamDialog() {
        val dialogBinding = DialogAddExamBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_exam)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogBinding.editExamName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch { examModel.addExam(name) }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
