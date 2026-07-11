package com.dyk1323.booklogs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
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
    indices = [Index(value = ["book_id"]), Index(value = ["reading_round_id"])],
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    /** Always non-null — a review always belongs to a specific reading round (docs/PLAN.md #6). */
    @ColumnInfo(name = "reading_round_id") val readingRoundId: Long,
    val content: String,
    val rating: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
