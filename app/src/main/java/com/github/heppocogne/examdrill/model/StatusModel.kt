package com.github.heppocogne.examdrill.model

import android.content.Context
import com.github.heppocogne.examdrill.db.AppDatabase
import com.github.heppocogne.examdrill.entity.StatusEntity
import kotlinx.coroutines.flow.Flow

class StatusModel(context: Context) {
    private val statusDao = AppDatabase.getInstance(context).statusDao()

    fun getAllStatuses(): Flow<List<StatusEntity>> = statusDao.getAll()
}
