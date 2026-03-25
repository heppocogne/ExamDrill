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

    @Query(
        "SELECT * FROM problems WHERE exam_id = :examId" +
                " AND (status_id IN (:statusIds) OR status_id IS NULL)" +
                " AND (reason_id IN (:reasonIds) OR reason_id IS NULL)" +
                " ORDER BY CASE WHEN last_quiz_date IS NULL THEN 0 ELSE 1 END, last_quiz_date ASC"
    )
    suspend fun getForReview(examId: Int, statusIds: List<Int>, reasonIds: List<Int>): List<ProblemEntity>

    @Query("SELECT * FROM problems WHERE exam_id = :examId ORDER BY RANDOM()")
    suspend fun getForRandom(examId: Int): List<ProblemEntity>

    @Insert
    suspend fun insert(entity: ProblemEntity)

    @Update
    suspend fun update(entity: ProblemEntity)

    @Delete
    suspend fun delete(entity: ProblemEntity)
}
