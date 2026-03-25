package com.github.heppocogne.examdrill

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.heppocogne.examdrill.databinding.ActivityMainBinding
import com.github.heppocogne.examdrill.databinding.DialogAddExamBinding
import com.github.heppocogne.examdrill.model.ExamModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var examModel: ExamModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        examModel = ExamModel(applicationContext)

        binding.fabAddExam.setOnClickListener { showAddExamDialog() }

        observeExams()
    }

    private fun observeExams() {
        lifecycleScope.launch {
            examModel.getAllExams().collect { exams ->
                binding.examList.removeAllViews()
                for (exam in exams) {
                    val button = Button(this@MainActivity).apply {
                        text = exam.name
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        ).apply { bottomMargin = 8 }
                        setOnClickListener {
                            // TODO: navigate to exam detail
                        }
                    }
                    binding.examList.addView(button)
                }
            }
        }
    }

    private fun showAddExamDialog() {
        val dialogBinding = DialogAddExamBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_exam)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogBinding.editExamName.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch { examModel.addExam(name) }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
