package com.github.heppocogne.examdrill.model

import android.content.Context
import com.github.heppocogne.examdrill.db.AppDatabase
import com.github.heppocogne.examdrill.entity.ReasonEntity
import kotlinx.coroutines.flow.Flow

class ReasonModel(context: Context) {
    private val reasonDao = AppDatabase.getInstance(context).reasonDao()

    fun getAllReasons(): Flow<List<ReasonEntity>> = reasonDao.getAll()

    suspend fun addReason(text: String) {
        reasonDao.insert(ReasonEntity(text = text))
    }
}
