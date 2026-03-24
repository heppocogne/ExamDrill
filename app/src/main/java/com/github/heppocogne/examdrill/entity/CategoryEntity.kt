package com.github.heppocogne.examdrill.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["exam_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CategoryEntity(
    @PrimaryKey
    val id: Int = 0,
    val text: String,
    @ColumnInfo("exam_id")
    val examId: Int,
)
