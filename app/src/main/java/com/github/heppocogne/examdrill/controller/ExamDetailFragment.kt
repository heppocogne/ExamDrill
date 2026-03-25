package com.github.heppocogne.examdrill.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.databinding.FragmentExamDetailBinding
import com.github.heppocogne.examdrill.model.ProblemModel
import kotlinx.coroutines.launch

class ExamDetailFragment : Fragment() {

    private var _binding: FragmentExamDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var problemModel: ProblemModel
    private lateinit var problemAdapter: ProblemAdapter

    private val examId: Int by lazy {
        requireArguments().getInt(ARG_EXAM_ID)
    }
    private val examName: String by lazy {
        requireArguments().getString(ARG_EXAM_NAME, "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExamDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        problemModel = ProblemModel(requireContext().applicationContext)

        requireActivity().title = examName

        problemAdapter = ProblemAdapter { problem ->
            // TODO: navigate to problem detail
        }
        binding.problemList.layoutManager = LinearLayoutManager(requireContext())
        binding.problemList.adapter = problemAdapter

        binding.btnRandomQuiz.setOnClickListener {
            // TODO: navigate to quiz screen (random)
        }

        binding.btnReview.setOnClickListener {
            // TODO: navigate to quiz screen (review)
        }

        binding.fabAddProblem.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProblemFragment.newInstance(examId))
                .addToBackStack(null)
                .commit()
        }

        observeProblems()
    }

    private fun observeProblems() {
        viewLifecycleOwner.lifecycleScope.launch {
            problemModel.getProblemsForExam(examId).collect { problems ->
                problemAdapter.submitList(problems)
                binding.textEmpty.visibility = if (problems.isEmpty()) VISIBLE else GONE
                binding.problemList.visibility = if (problems.isEmpty()) GONE else VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EXAM_ID = "exam_id"
        const val ARG_EXAM_NAME = "exam_name"

        fun newInstance(examId: Int, examName: String): ExamDetailFragment {
            return ExamDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EXAM_ID, examId)
                    putString(ARG_EXAM_NAME, examName)
                }
            }
        }
    }
}
