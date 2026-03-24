package com.github.heppocogne.examdrill.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.heppocogne.examdrill.entity.StatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT COUNT(*) FROM status")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM status")
    fun getAll(): Flow<List<StatusEntity>>

    @Insert
    suspend fun insert(entity: StatusEntity)

    @Delete
    suspend fun delete(entity: StatusEntity)
}
