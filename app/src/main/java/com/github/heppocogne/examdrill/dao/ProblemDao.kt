package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.github.heppocogne.examdrill.entity.ProblemEntity

@Dao
interface ProblemDao {
    // TODO: SELECTを追加する
    
    @Insert
    suspend fun insert(entity: ProblemEntity)

    @Update
    suspend fun update(entity: ProblemEntity)

    @Delete
    suspend fun delete(entity: ProblemEntity)
}