package com.github.heppocogne.examdrill.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reasons")
data class ReasonEntity(
    @PrimaryKey
    val id: Int = 0,
    val text: String,
)
