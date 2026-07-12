package com.dyk1323.booklogs.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dyk1323.booklogs.data.local.dao.BookDao
import com.dyk1323.booklogs.data.local.dao.QuoteCommentDao
import com.dyk1323.booklogs.data.local.dao.QuoteDao
import com.dyk1323.booklogs.data.local.dao.ReadingLogDao
import com.dyk1323.booklogs.data.local.dao.ReadingRoundDao
import com.dyk1323.booklogs.data.local.dao.ReviewDao
import com.dyk1323.booklogs.data.local.entity.BookEntity
import com.dyk1323.booklogs.data.local.entity.QuoteCommentEntity
import com.dyk1323.booklogs.data.local.entity.QuoteEntity
import com.dyk1323.booklogs.data.local.entity.ReadingLogEntity
import com.dyk1323.booklogs.data.local.entity.ReadingRoundEntity
import com.dyk1323.booklogs.data.local.entity.ReviewEntity

/**
 * Schema started at version 1 with that day's final field set (see docs/PLAN.md "마이그레이션 원칙") —
 * every schema change since ships a real [androidx.room.migration.Migration] (see [MIGRATION_1_2]),
 * never `fallbackToDestructiveMigration()`, since this is the user's only copy of their reading history.
 */
@Database(
    entities = [
        BookEntity::class,
        ReadingRoundEntity::class,
        ReadingLogEntity::class,
        QuoteEntity::class,
        ReviewEntity::class,
        QuoteCommentEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class BooklogsDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingRoundDao(): ReadingRoundDao
    abstract fun readingLogDao(): ReadingLogDao
    abstract fun quoteDao(): QuoteDao
    abstract fun reviewDao(): ReviewDao
    abstract fun quoteCommentDao(): QuoteCommentDao

    companion object {
        const val DATABASE_NAME = "booklogs.db"
    }
}
