package com.github.heppocogne.examdrill.model

import android.content.Context
import com.github.heppocogne.examdrill.db.AppDatabase
import com.github.heppocogne.examdrill.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

class ProblemModel(context: Context) {
    private val problemDao = AppDatabase.getInstance(context).problemDao()

    fun getProblemsForExam(examId: Int): Flow<List<ProblemEntity>> =
        problemDao.getByExamId(examId)

    suspend fun getProblemById(id: Int): ProblemEntity? =
        problemDao.getById(id)

    suspend fun addProblem(problem: ProblemEntity) {
        problemDao.insert(problem)
    }

    suspend fun getForReview(examId: Int, statusIds: List<Int>, reasonIds: List<Int>): List<ProblemEntity> =
        problemDao.getForReview(examId, statusIds, reasonIds)

    suspend fun getForRandom(examId: Int): List<ProblemEntity> =
        problemDao.getForRandom(examId)

    suspend fun updateProblem(problem: ProblemEntity) {
        problemDao.update(problem)
    }
}
