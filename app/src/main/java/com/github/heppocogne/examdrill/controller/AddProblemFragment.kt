package com.github.heppocogne.examdrill.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.databinding.DialogAddCategoryBinding
import com.github.heppocogne.examdrill.databinding.DialogAddReasonBinding
import com.github.heppocogne.examdrill.databinding.FragmentAddProblemBinding
import com.github.heppocogne.examdrill.entity.CategoryEntity
import com.github.heppocogne.examdrill.entity.ProblemEntity
import com.github.heppocogne.examdrill.entity.ReasonEntity
import com.github.heppocogne.examdrill.entity.StatusEntity
import com.github.heppocogne.examdrill.model.CategoryModel
import com.github.heppocogne.examdrill.model.ProblemModel
import com.github.heppocogne.examdrill.model.ReasonModel
import com.github.heppocogne.examdrill.model.StatusModel
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Date

class AddProblemFragment : Fragment() {

    private var _binding: FragmentAddProblemBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryModel: CategoryModel
    private lateinit var reasonModel: ReasonModel
    private lateinit var statusModel: StatusModel
    private lateinit var problemModel: ProblemModel

    private var categories: List<CategoryEntity> = emptyList()
    private var reasons: List<ReasonEntity> = emptyList()
    private var statuses: List<StatusEntity> = emptyList()
    private var selectedCategoryId: Int? = null
    private var selectedReasonId: Int? = null
    private var selectedStatusId: Int? = null

    private val examId: Int by lazy { requireArguments().getInt(ARG_EXAM_ID) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddProblemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContext = requireContext().applicationContext
        categoryModel = CategoryModel(appContext)
        reasonModel = ReasonModel(appContext)
        statusModel = StatusModel(appContext)
        problemModel = ProblemModel(appContext)

        requireActivity().title = getString(R.string.add_problem)

        setupChoiceDropdowns()
        observeCategories()
        observeReasons()
        observeStatuses()

        binding.btnAddCategory.setOnClickListener { showAddCategoryDialog() }
        binding.btnAddReason.setOnClickListener { showAddReasonDialog() }
        binding.btnSave.setOnClickListener { saveProblem() }
    }

    private fun setupChoiceDropdowns() {
        val choices = resources.getStringArray(R.array.choices)
        val choiceAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, choices)
        binding.dropdownUserChoice.setAdapter(choiceAdapter)
        binding.dropdownCorrectAnswer.setAdapter(choiceAdapter)
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryModel.getCategoriesForExam(examId).collect { list ->
                val sorted = list.sortedBy { it.text }
                categories = sorted
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    sorted.map { it.text },
                )
                binding.dropdownCategory.setAdapter(adapter)
                binding.dropdownCategory.setOnItemClickListener { _, _, position, _ ->
                    selectedCategoryId = sorted[position].id
                }
            }
        }
    }

    private fun observeReasons() {
        viewLifecycleOwner.lifecycleScope.launch {
            reasonModel.getAllReasons().collect { list ->
                reasons = list
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    list.map { it.text },
                )
                binding.dropdownReason.setAdapter(adapter)
                binding.dropdownReason.setOnItemClickListener { _, _, position, _ ->
                    selectedReasonId = list[position].id
                }
            }
        }
    }

    private fun observeStatuses() {
        viewLifecycleOwner.lifecycleScope.launch {
            statusModel.getAllStatuses().collect { list ->
                statuses = list
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    list.map { it.text },
                )
                binding.dropdownStatus.setAdapter(adapter)
                binding.dropdownStatus.setOnItemClickListener { _, _, position, _ ->
                    selectedStatusId = list[position].id
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_category)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogBinding.editCategoryName.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (categories.any { it.text.equals(name, ignoreCase = true) }) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.duplicate_entry, name),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        viewLifecycleOwner.lifecycleScope.launch {
                            categoryModel.addCategory(name, examId)
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddReasonDialog() {
        val dialogBinding = DialogAddReasonBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_reason)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogBinding.editReasonName.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (reasons.any { it.text.equals(name, ignoreCase = true) }) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.duplicate_entry, name),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        viewLifecycleOwner.lifecycleScope.launch {
                            reasonModel.addReason(name)
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveProblem() {
        val problemText = binding.editProblemText.text.toString().trim()
        val choiceA = binding.editChoiceA.text.toString().trim()
        val choiceB = binding.editChoiceB.text.toString().trim()
        val choiceC = binding.editChoiceC.text.toString().trim()
        val choiceD = binding.editChoiceD.text.toString().trim()
        val userChoice = binding.dropdownUserChoice.text.toString().trim()
        val correctAnswer = binding.dropdownCorrectAnswer.text.toString().trim()
        val explanation = binding.editExplanation.text.toString().trim()

        val missingField = when {
            selectedCategoryId == null -> getString(R.string.category)
            problemText.isEmpty() -> getString(R.string.problem_text)
            choiceA.isEmpty() -> getString(R.string.choice_a)
            choiceB.isEmpty() -> getString(R.string.choice_b)
            choiceC.isEmpty() -> getString(R.string.choice_c)
            choiceD.isEmpty() -> getString(R.string.choice_d)
            userChoice.isEmpty() -> getString(R.string.user_choice)
            correctAnswer.isEmpty() -> getString(R.string.correct_answer)
            else -> null
        }
        if (missingField != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.field_required, missingField),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val now = Date()
        val problem = ProblemEntity(
            examId = examId,
            categoryId = selectedCategoryId!!,
            text = problemText,
            choiceA = choiceA,
            choiceB = choiceB,
            choiceC = choiceC,
            choiceD = choiceD,
            userChoice = userChoice,
            answer = correctAnswer,
            reasonId = selectedReasonId,
            statusId = selectedStatusId,
            explanation = explanation,
            createdDate = now,
            updatedDate = now,
        )

        viewLifecycleOwner.lifecycleScope.launch {
            problemModel.addProblem(problem)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EXAM_ID = "ARG_EXAM_ID"

        fun newInstance(examId: Int): AddProblemFragment {
            return AddProblemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EXAM_ID, examId)
                }
            }
        }
    }
}
