package com.github.heppocogne.examdrill.controller

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.databinding.FragmentQuizBinding
import com.github.heppocogne.examdrill.entity.ProblemEntity
import com.github.heppocogne.examdrill.entity.StatusEntity
import com.github.heppocogne.examdrill.model.ProblemModel
import com.github.heppocogne.examdrill.model.StatusModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var problemModel: ProblemModel
    private lateinit var statusModel: StatusModel

    private var problems: List<ProblemEntity> = emptyList()
    private var statuses: List<StatusEntity> = emptyList()
    private var currentIndex = 0
    private var answered = false
    private var selectedStatusId: Int? = null

    private val choiceLabels = arrayOf("ア", "イ", "ウ", "エ")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContext = requireContext().applicationContext
        problemModel = ProblemModel(appContext)
        statusModel = StatusModel(appContext)

        problems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_PROBLEMS, ProblemEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelableArrayList(ARG_PROBLEMS)
        } ?: emptyList()

        problems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_PROBLEMS, ProblemEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelableArrayList(ARG_PROBLEMS)
        } ?: emptyList()

        binding.btnAction.setOnClickListener { onActionClick() }
        binding.btnEdit.setOnClickListener { onEditClick() }
        setupRadioExclusion()

        parentFragmentManager.setFragmentResultListener(RESULT_PROBLEM_EDITED, viewLifecycleOwner) { _, _ ->
            reloadCurrentProblem()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            statuses = statusModel.getAllStatuses().first()
            setupStatusDropdown()
            showProblem()
        }
    }

    private fun setupStatusDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statuses.map { it.text },
        )
        binding.dropdownStatus.setAdapter(adapter)
        binding.dropdownStatus.setOnItemClickListener { _, _, position, _ ->
            selectedStatusId = statuses[position].id
        }
    }

    private fun showProblem() {
        if (currentIndex >= problems.size) return

        val problem = problems[currentIndex]
        answered = false
        selectedStatusId = problem.statusId

        binding.textProgress.text =
            getString(R.string.quiz_progress, currentIndex + 1, problems.size)
        binding.textProblem.text = problem.text

        val choices = resources.getStringArray(R.array.choices)
        binding.textLabelA.text = choices[0]
        binding.textLabelB.text = choices[1]
        binding.textLabelC.text = choices[2]
        binding.textLabelD.text = choices[3]
        binding.textChoiceA.text = problem.choiceA
        binding.textChoiceB.text = problem.choiceB
        binding.textChoiceC.text = problem.choiceC
        binding.textChoiceD.text = problem.choiceD

        clearRadioChecks()
        setRadioEnabled(true)
        binding.resultArea.visibility = View.GONE
        binding.btnAction.setText(R.string.answer_button)

        // 理解度ドロップダウンに現在値をセット
        val currentStatus = statuses.find { it.id == problem.statusId }
        binding.dropdownStatus.setText(currentStatus?.text ?: "", false)
    }

    private fun onEditClick() {
        if (currentIndex >= problems.size) return
        val problem = problems[currentIndex]
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddProblemFragment.newInstanceForEdit(problem))
            .addToBackStack(null)
            .commit()
    }

    private fun reloadCurrentProblem() {
        if (currentIndex >= problems.size) return
        val problemId = problems[currentIndex].id
        viewLifecycleOwner.lifecycleScope.launch {
            val updated = problemModel.getProblemById(problemId)
            if (updated != null) {
                problems = problems.toMutableList().also { it[currentIndex] = updated }
                showProblem()
            }
        }
    }

    private fun onActionClick() {
        if (answered) {
            onNext()
        } else {
            onAnswer()
        }
    }

    private fun onAnswer() {
        val selectedChoiceId = getCheckedRadioButtonId()
        if (selectedChoiceId == -1) {
            Toast.makeText(requireContext(), R.string.select_choice, Toast.LENGTH_SHORT).show()
            return
        }

        val selectedLabel = when (selectedChoiceId) {
            R.id.radio_choice_a -> choiceLabels[0]
            R.id.radio_choice_b -> choiceLabels[1]
            R.id.radio_choice_c -> choiceLabels[2]
            R.id.radio_choice_d -> choiceLabels[3]
            else -> ""
        }

        val problem = problems[currentIndex]
        val isCorrect = selectedLabel == problem.answer

        if (isCorrect) {
            binding.textResult.text = getString(R.string.result_correct)
            binding.textResult.setTextColor(requireContext().getColor(R.color.correct))
        } else {
            binding.textResult.text = getString(R.string.result_incorrect, problem.answer)
            binding.textResult.setTextColor(requireContext().getColor(R.color.incorrect))
        }

        binding.editExplanation.setText(problem.explanation)
        binding.resultArea.visibility = View.VISIBLE
        setRadioEnabled(false)
        binding.btnAction.setText(R.string.next_problem)
        answered = true

        binding.scrollContent.post {
            binding.scrollContent.smoothScrollTo(0, binding.resultArea.top)
        }

        // lastQuizDateを更新
        viewLifecycleOwner.lifecycleScope.launch {
            problemModel.updateProblem(problem.copy(lastQuizDate = Date(), updatedDate = Date()))
        }
    }

    private fun onNext() {
        // 回答後の変更を保存
        if (answered) {
            saveChanges()
        }

        currentIndex++
        if (currentIndex >= problems.size) {
            Toast.makeText(requireContext(), R.string.quiz_finished, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        showProblem()
    }

    private fun saveChanges() {
        val problem = problems[currentIndex]
        val newExplanation = binding.editExplanation.text.toString().trim()
        if (newExplanation != problem.explanation || selectedStatusId != problem.statusId) {
            val updated = problem.copy(
                explanation = newExplanation,
                statusId = selectedStatusId,
                updatedDate = Date(),
            )
            viewLifecycleOwner.lifecycleScope.launch {
                problemModel.updateProblem(updated)
            }
        }
    }

    private val radioButtons
        get() = listOf(
            binding.radioChoiceA,
            binding.radioChoiceB,
            binding.radioChoiceC,
            binding.radioChoiceD,
        )

    private val choiceRows
        get() = listOf(
            Triple(binding.textLabelA, binding.radioChoiceA, binding.textChoiceA),
            Triple(binding.textLabelB, binding.radioChoiceB, binding.textChoiceB),
            Triple(binding.textLabelC, binding.radioChoiceC, binding.textChoiceC),
            Triple(binding.textLabelD, binding.radioChoiceD, binding.textChoiceD),
        )

    private fun setupRadioExclusion() {
        for ((label, rb, choiceText) in choiceRows) {
            val onClick = View.OnClickListener {
                rb.isChecked = true
                for (other in radioButtons) {
                    if (other != rb) other.isChecked = false
                }
            }
            rb.setOnClickListener(onClick)
            label.setOnClickListener(onClick)
            choiceText.setOnClickListener(onClick)
        }
    }

    private fun clearRadioChecks() {
        for (rb in radioButtons) {
            rb.isChecked = false
        }
    }

    private fun getCheckedRadioButtonId(): Int {
        return radioButtons.firstOrNull { it.isChecked }?.id ?: -1
    }

    private fun setRadioEnabled(enabled: Boolean) {
        binding.radioChoiceA.isEnabled = enabled
        binding.radioChoiceB.isEnabled = enabled
        binding.radioChoiceC.isEnabled = enabled
        binding.radioChoiceD.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PROBLEMS = "ARG_PROBLEMS"
        const val RESULT_PROBLEM_EDITED = "RESULT_PROBLEM_EDITED"

        fun newInstance(problems: List<ProblemEntity>): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PROBLEMS, ArrayList(problems))
                }
            }
        }
    }
}
