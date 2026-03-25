package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.heppocogne.examdrill.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems WHERE exam_id = :examId")
    fun getByExamId(examId: Int): Flow<List<ProblemEntity>>

    @Insert
    suspend fun insert(entity: ProblemEntity)

    @Update
    suspend fun update(entity: ProblemEntity)

    @Delete
    suspend fun delete(entity: ProblemEntity)
}
