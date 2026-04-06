package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.heppocogne.examdrill.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE exam_id = :examId")
    fun getCategories(examId: Int): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(entity: CategoryEntity)

    @Update
    suspend fun update(entity: CategoryEntity)

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("SELECT COUNT(*) FROM problems WHERE category_id = :categoryId")
    suspend fun countProblemsByCategory(categoryId: Int): Int
}
