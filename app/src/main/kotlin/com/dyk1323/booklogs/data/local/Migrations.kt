package com.dyk1323.booklogs.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the `quote_comments` table (인용구 댓글, docs/PLAN.md "인용구 댓글"). Hand-written to match exactly
 * what Room would generate for [com.dyk1323.booklogs.data.local.entity.QuoteCommentEntity] — see
 * schemas/.../1.json's `quotes` entry for the same `FOREIGN KEY ... ON DELETE CASCADE` / index style this
 * mirrors. Real migration, not `fallbackToDestructiveMigration()`, per docs/PLAN.md "마이그레이션 원칙".
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `quote_comments` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`quote_id` INTEGER NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "FOREIGN KEY(`quote_id`) REFERENCES `quotes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_quote_comments_quote_id` ON `quote_comments` (`quote_id`)",
        )
    }
}
