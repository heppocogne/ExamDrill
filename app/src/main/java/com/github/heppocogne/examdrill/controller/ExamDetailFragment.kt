package com.github.heppocogne.examdrill.controller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.heppocogne.examdrill.R
import com.github.heppocogne.examdrill.databinding.FragmentExamDetailBinding
import com.github.heppocogne.examdrill.entity.ProblemEntity
import com.github.heppocogne.examdrill.model.CategoryModel
import com.github.heppocogne.examdrill.model.ProblemModel
import kotlinx.coroutines.launch

class ExamDetailFragment : Fragment() {

    private var _binding: FragmentExamDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var problemModel: ProblemModel
    private lateinit var categoryModel: CategoryModel
    private lateinit var problemAdapter: ProblemAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var allProblems: List<ProblemEntity> = emptyList()

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
        categoryModel = CategoryModel(requireContext().applicationContext)

        requireActivity().title = examName

        setupDrawer()

        problemAdapter = ProblemAdapter { problem ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizFragment.newInstance(listOf(problem)))
                .addToBackStack(null)
                .commit()
        }
        binding.problemList.layoutManager = LinearLayoutManager(requireContext())
        binding.problemList.adapter = problemAdapter

        binding.fabAddProblem.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProblemFragment.newInstance(examId))
                .addToBackStack(null)
                .commit()
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter(s?.toString().orEmpty())
            }
        })

        observeCategories()
        observeProblems()
    }

    private fun setupDrawer() {
        val activity = requireActivity() as AppCompatActivity
        val toolbar =
            activity.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        drawerToggle = ActionBarDrawerToggle(
            activity,
            binding.drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name,
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.menu_quiz -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, QuizSettingsFragment.newInstance(examId))
                        .addToBackStack(null)
                        .commit()
                    true
                }

                R.id.menu_edit_categories -> {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            EditCategoriesFragment.newInstance(examId)
                        )
                        .addToBackStack(null)
                        .commit()
                    true
                }

                R.id.menu_add_problem -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AddProblemFragment.newInstance(examId))
                        .addToBackStack(null)
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    private fun applyFilter(query: String) {
        val words = query.split(Regex("[\\s　]+")).filter { it.isNotEmpty() }
        val filtered = if (words.isEmpty()) {
            allProblems
        } else {
            allProblems.filter { problem ->
                words.all { word ->
                    problem.text.contains(word, ignoreCase = true) ||
                            problem.choiceA.contains(word, ignoreCase = true) ||
                            problem.choiceB.contains(word, ignoreCase = true) ||
                            problem.choiceC.contains(word, ignoreCase = true) ||
                            problem.choiceD.contains(word, ignoreCase = true)
                }
            }
        }
        problemAdapter.submitList(filtered)
        val isEmpty = filtered.isEmpty()
        binding.textEmpty.visibility = if (isEmpty) VISIBLE else GONE
        binding.problemList.visibility = if (isEmpty) GONE else VISIBLE
        if (isEmpty) {
            val isSearching = words.isNotEmpty()
            binding.textEmpty.setText(
                if (isSearching) R.string.no_search_results else R.string.no_problems
            )
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryModel.getCategoriesForExam(examId).collect { categories ->
                problemAdapter.categoryMap = categories.associate { it.id to it.text }
                problemAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeProblems() {
        viewLifecycleOwner.lifecycleScope.launch {
            problemModel.getProblemsForExam(examId).collect { problems ->
                // TODO: 全件取得をやめる
                allProblems = problems
                applyFilter(binding.editSearch.text?.toString().orEmpty())
            }
        }
    }

    override fun onDestroyView() {
        binding.drawerLayout.removeDrawerListener(drawerToggle)
        val activity = requireActivity() as AppCompatActivity
        val toolbar =
            activity.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.navigationIcon = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EXAM_ID = "ARG_EXAM_ID"
        const val ARG_EXAM_NAME = "ARG_EXAM_NAME"

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
