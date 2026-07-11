package com.dyk1323.booklogs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A plain progress snapshot — no delta column. Page-delta is always derived on demand (see
 * com.dyk1323.booklogs.domain.usecase.computeLogDeltas) so editing/deleting any single row never
 * requires touching its neighbors.
 */
@Entity(
    tableName = "reading_logs",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ReadingRoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["reading_round_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["book_id"]), Index(value = ["reading_round_id"]), Index(value = ["log_date_epoch_day"])],
)
data class ReadingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "reading_round_id") val readingRoundId: Long,
    @ColumnInfo(name = "current_page") val currentPage: Int,
    @ColumnInfo(name = "log_date_epoch_day") val logDateEpochDay: Long,
    @ColumnInfo(name = "logged_at") val loggedAt: Long,
)
