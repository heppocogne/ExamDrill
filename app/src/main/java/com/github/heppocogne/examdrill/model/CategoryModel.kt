package com.github.heppocogne.examdrill.model

import android.content.Context
import com.github.heppocogne.examdrill.db.AppDatabase
import com.github.heppocogne.examdrill.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryModel(context: Context) {
    private val categoryDao = AppDatabase.getInstance(context).categoryDao()

    fun getCategoriesForExam(examId: Int): Flow<List<CategoryEntity>> =
        categoryDao.getCategories(examId)

    suspend fun addCategory(text: String, examId: Int) {
        categoryDao.insert(CategoryEntity(text = text, examId = examId))
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    suspend fun countProblemsByCategory(categoryId: Int): Int =
        categoryDao.countProblemsByCategory(categoryId)
}
