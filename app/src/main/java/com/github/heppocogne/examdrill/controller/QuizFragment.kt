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

        binding.btnAction.setOnClickListener { onActionClick() }

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

        binding.radioChoiceA.text = getString(R.string.choice_a) + "：" + problem.choiceA
        binding.radioChoiceB.text = getString(R.string.choice_b) + "：" + problem.choiceB
        binding.radioChoiceC.text = getString(R.string.choice_c) + "：" + problem.choiceC
        binding.radioChoiceD.text = getString(R.string.choice_d) + "：" + problem.choiceD

        binding.radioGroupChoices.clearCheck()
        setRadioEnabled(true)
        binding.resultArea.visibility = View.GONE
        binding.btnAction.setText(R.string.answer_button)

        // 理解度ドロップダウンに現在値をセット
        val currentStatus = statuses.find { it.id == problem.statusId }
        binding.dropdownStatus.setText(currentStatus?.text ?: "", false)
    }

    private fun onActionClick() {
        if (answered) {
            onNext()
        } else {
            onAnswer()
        }
    }

    private fun onAnswer() {
        val selectedChoiceId = binding.radioGroupChoices.checkedRadioButtonId
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

        fun newInstance(problems: List<ProblemEntity>): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PROBLEMS, ArrayList(problems))
                }
            }
        }
    }
}
