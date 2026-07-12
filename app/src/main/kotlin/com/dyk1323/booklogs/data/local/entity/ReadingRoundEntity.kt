package com.dyk1323.booklogs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_rounds",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["book_id"])],
)
data class ReadingRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "round_number") val roundNumber: Int,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "finished_at") val finishedAt: Long?,
    /** Serialized [com.dyk1323.booklogs.domain.model.RoundEndReason] name, null while the round is open. */
    @ColumnInfo(name = "end_reason") val endReason: String?,
    /** Delta baseline for this round's first log — see [com.dyk1323.booklogs.domain.usecase.computeLogDeltas]. */
    @ColumnInfo(name = "starting_page", defaultValue = "0") val startingPage: Int = 0,
)
