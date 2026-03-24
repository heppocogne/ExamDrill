package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.heppocogne.examdrill.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query(value = "SELECT COUNT(*) FROM examinations")
    fun getCount(): Flow<Int>

    @Query(value = "SELECT * FROM examinations")
    fun getAll(): Flow<List<ExamEntity>>

    @Insert
    suspend fun insert(entity: ExamEntity)

    @Delete
    suspend fun delete(entity: ExamEntity)
}
