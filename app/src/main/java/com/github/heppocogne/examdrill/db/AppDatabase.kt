package com.github.heppocogne.examdrill.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.heppocogne.examdrill.dao.CategoryDao
import com.github.heppocogne.examdrill.dao.ExamDao
import com.github.heppocogne.examdrill.dao.ProblemDao
import com.github.heppocogne.examdrill.dao.ReasonDao
import com.github.heppocogne.examdrill.dao.StatusDao
import com.github.heppocogne.examdrill.entity.CategoryEntity
import com.github.heppocogne.examdrill.entity.ExamEntity
import com.github.heppocogne.examdrill.entity.ProblemEntity
import com.github.heppocogne.examdrill.entity.ReasonEntity
import com.github.heppocogne.examdrill.entity.StatusEntity

@Database(
    entities = [
        ExamEntity::class,
        CategoryEntity::class,
        ProblemEntity::class,
        ReasonEntity::class,
        StatusEntity::class,
    ],
    version = 1,
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun examDao(): ExamDao
    abstract fun categoryDao(): CategoryDao
    abstract fun problemDao(): ProblemDao
    abstract fun reasonDao(): ReasonDao
    abstract fun statusDao(): StatusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exam_drill_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).statusDao().apply {
                                insert(StatusEntity(text = "未理解"))
                                insert(StatusEntity(text = "怪しい"))
                                insert(StatusEntity(text = "理解済み"))
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }
}
