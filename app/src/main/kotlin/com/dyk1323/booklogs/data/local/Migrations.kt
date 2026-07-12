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

/**
 * 라운드에 `starting_page`(델타 계산 기준점, docs/PLAN.md "라운드 이력 편집") 추가하고, `reviews`에서
 * `reading_round_id` FK를 완전히 제거해 독후감을 책 단위로 만든다 — 사용자 요청으로 로그는 여전히 라운드에
 * 묶이지만(델타 계산에 필요) 독후감은 더 이상 그럴 필요가 없다고 판단해 제거.
 *
 * `reading_rounds`는 컬럼 추가라 단순 `ALTER TABLE ... ADD COLUMN`으로 충분하지만, `reviews`는 FK가 걸린
 * 컬럼을 제거하는 구조 변경이라 SQLite/Room 권장 방식대로 새 테이블을 만들어 데이터를 옮기고 기존 테이블을
 * 지운 뒤 이름을 바꾸는 절차를 거친다. `reviews`를 참조하는 다른 테이블은 없으므로 FK 무결성 걱정 없이
 * 안전하게 재생성 가능.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `reading_rounds` ADD COLUMN `starting_page` INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reviews_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`book_id` INTEGER NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`rating` INTEGER, " +
                "`created_at` INTEGER NOT NULL, " +
                "FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL(
            "INSERT INTO `reviews_new` (`id`, `book_id`, `content`, `rating`, `created_at`) " +
                "SELECT `id`, `book_id`, `content`, `rating`, `created_at` FROM `reviews`",
        )
        db.execSQL("DROP TABLE `reviews`")
        db.execSQL("ALTER TABLE `reviews_new` RENAME TO `reviews`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_book_id` ON `reviews` (`book_id`)")
    }
}
