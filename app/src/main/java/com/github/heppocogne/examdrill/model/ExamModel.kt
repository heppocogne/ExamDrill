package com.github.heppocogne.examdrill.model

import android.content.Context
import com.github.heppocogne.examdrill.db.AppDatabase
import com.github.heppocogne.examdrill.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

class ExamModel(context: Context) {
    private val examDao = AppDatabase.getInstance(context).examDao()

    fun getAllExams(): Flow<List<ExamEntity>> = examDao.getAll()

    suspend fun addExam(name: String) {
        examDao.insert(ExamEntity(name = name))
    }
}
