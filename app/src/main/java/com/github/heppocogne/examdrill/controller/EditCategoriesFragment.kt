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
import com.github.heppocogne.examdrill.databinding.DialogAddCategoryBinding
import com.github.heppocogne.examdrill.databinding.FragmentEditCategoriesBinding
import com.github.heppocogne.examdrill.entity.CategoryEntity
import com.github.heppocogne.examdrill.model.CategoryModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class EditCategoriesFragment : Fragment() {

    private var _binding: FragmentEditCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryModel: CategoryModel
    private lateinit var categoryAdapter: CategoryAdapter

    private val examId: Int by lazy { requireArguments().getInt(ARG_EXAM_ID) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryModel = CategoryModel(requireContext().applicationContext)

        requireActivity().title = getString(R.string.edit_categories)

        categoryAdapter = CategoryAdapter(
            onEdit = { category -> showEditDialog(category) },
            onDelete = { category -> confirmDelete(category) },
        )
        binding.categoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryList.adapter = categoryAdapter

        observeCategories()
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryModel.getCategoriesForExam(examId).collect { categories ->
                val sorted = categories.sortedBy { it.text }
                categoryAdapter.submitList(sorted)
                val isEmpty = sorted.isEmpty()
                binding.textEmpty.visibility = if (isEmpty) VISIBLE else GONE
                binding.categoryList.visibility = if (isEmpty) GONE else VISIBLE
            }
        }
    }

    private fun showEditDialog(category: CategoryEntity) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        dialogBinding.editCategoryName.setText(category.text)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_category)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = dialogBinding.editCategoryName.text.toString().trim()
                if (newName.isNotEmpty() && newName != category.text) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        categoryModel.updateCategory(category.copy(text = newName))
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(category: CategoryEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            val count = categoryModel.countProblemsByCategory(category.id)

            if (count == 0) {
                categoryModel.deleteCategory(category)
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_category)
                    .setMessage(getString(R.string.delete_category_warning, count))
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            categoryModel.deleteCategory(category)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EXAM_ID = "ARG_EXAM_ID"

        fun newInstance(examId: Int): EditCategoriesFragment {
            return EditCategoriesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EXAM_ID, examId)
                }
            }
        }
    }
}
