package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.heppocogne.examdrill.entity.ReasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReasonDao {
    @Query("SELECT COUNT(*) FROM reasons")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM reasons")
    fun getAll(): Flow<List<ReasonEntity>>

    @Insert
    suspend fun insert(entity: ReasonEntity)

    @Delete
    suspend fun delete(entity: ReasonEntity)
}
