package com.github.heppocogne.examdrill.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.databinding.FragmentQuizSettingsBinding
import com.github.heppocogne.examdrill.entity.ReasonEntity
import com.github.heppocogne.examdrill.entity.StatusEntity
import com.github.heppocogne.examdrill.model.ProblemModel
import com.github.heppocogne.examdrill.model.ReasonModel
import com.github.heppocogne.examdrill.model.StatusModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuizSettingsFragment : Fragment() {

    private var _binding: FragmentQuizSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var statusModel: StatusModel
    private lateinit var reasonModel: ReasonModel
    private lateinit var problemModel: ProblemModel

    private var statuses: List<StatusEntity> = emptyList()
    private var reasons: List<ReasonEntity> = emptyList()
    private val statusCheckBoxes = mutableListOf<Pair<CheckBox, StatusEntity>>()
    private val reasonCheckBoxes = mutableListOf<Pair<CheckBox, ReasonEntity>>()

    private val examId: Int by lazy { requireArguments().getInt(ARG_EXAM_ID) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuizSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContext = requireContext().applicationContext
        statusModel = StatusModel(appContext)
        reasonModel = ReasonModel(appContext)
        problemModel = ProblemModel(appContext)

        requireActivity().title = getString(R.string.quiz_settings)

        loadFilters()

        binding.radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            binding.reviewFilters.visibility =
                if (checkedId == R.id.radio_review) View.VISIBLE else View.GONE
        }

        binding.btnResetDefaults.setOnClickListener { applyDefaults() }
        binding.btnStartQuiz.setOnClickListener { startQuiz() }
    }

    private fun loadFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            statuses = statusModel.getAllStatuses().first()
            reasons = reasonModel.getAllReasons().first()
            buildStatusCheckBoxes()
            buildReasonCheckBoxes()
            applyDefaults()
        }
    }

    private fun buildStatusCheckBoxes() {
        binding.statusCheckboxes.removeAllViews()
        statusCheckBoxes.clear()
        for (status in statuses) {
            val cb = CheckBox(requireContext()).apply { text = status.text }
            statusCheckBoxes.add(cb to status)
            binding.statusCheckboxes.addView(cb)
        }
    }

    private fun buildReasonCheckBoxes() {
        binding.reasonCheckboxes.removeAllViews()
        reasonCheckBoxes.clear()
        for (reason in reasons) {
            val cb = CheckBox(requireContext()).apply { text = reason.text }
            reasonCheckBoxes.add(cb to reason)
            binding.reasonCheckboxes.addView(cb)
        }
    }

    private fun applyDefaults() {
        binding.radioReview.isChecked = true
        binding.reviewFilters.visibility = View.VISIBLE

        // 理解度: 「理解済み」以外をチェック
        for ((cb, status) in statusCheckBoxes) {
            cb.isChecked = status.text != "理解済み"
        }
        // 間違えた理由: 全てチェック
        for ((cb, _) in reasonCheckBoxes) {
            cb.isChecked = true
        }
    }

    private fun startQuiz() {
        val isReview = binding.radioReview.isChecked

        viewLifecycleOwner.lifecycleScope.launch {
            val problems = if (isReview) {
                val selectedStatusIds = statusCheckBoxes
                    .filter { it.first.isChecked }
                    .map { it.second.id }
                val selectedReasonIds = reasonCheckBoxes
                    .filter { it.first.isChecked }
                    .map { it.second.id }
                problemModel.getForReview(examId, selectedStatusIds, selectedReasonIds)
            } else {
                problemModel.getForRandom(examId)
            }

            if (problems.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_matching_problems, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizFragment.newInstance(problems))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EXAM_ID = "ARG_EXAM_ID"

        fun newInstance(examId: Int): QuizSettingsFragment {
            return QuizSettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EXAM_ID, examId)
                }
            }
        }
    }
}
