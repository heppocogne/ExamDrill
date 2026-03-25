package com.github.heppocogne.examdrill.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize  // TODO: IDだけ保持するようにすれば要らなくなるはず
@Entity(
    tableName = "problems",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["exam_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ReasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["reason_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StatusEntity::class,
            parentColumns = ["id"],
            childColumns = ["status_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class ProblemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "exam_id")
    val examId: Int,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    val text: String,
    @ColumnInfo(name = "choice_a")
    val choiceA: String,
    @ColumnInfo(name = "choice_b")
    val choiceB: String,
    @ColumnInfo(name = "choice_c")
    val choiceC: String,
    @ColumnInfo(name = "choice_d")
    val choiceD: String,
    @ColumnInfo(name = "user_choice")
    val userChoice: String,
    val answer: String,
    @ColumnInfo(name = "reason_id")
    val reasonId: Int? = null,
    @ColumnInfo(name = "status_id")
    val statusId: Int? = null,
    val explanation: String,
    val createdDate: Date,
    val updatedDate: Date,
    @ColumnInfo(name = "last_quiz_date")
    val lastQuizDate: Date? = null,
    // TODO: 正答回数・誤答回数を記録する
) : Parcelable
