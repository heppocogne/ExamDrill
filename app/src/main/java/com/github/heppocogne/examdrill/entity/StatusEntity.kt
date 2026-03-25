package com.github.heppocogne.examdrill.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("status")
data class StatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
)
