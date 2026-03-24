package com.github.heppocogne.examdrill.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "examinations")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)
