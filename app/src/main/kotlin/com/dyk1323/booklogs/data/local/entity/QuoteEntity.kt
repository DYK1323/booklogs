package com.dyk1323.booklogs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotes",
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
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    val text: String,
    @ColumnInfo(name = "page_number") val pageNumber: Int?,
    /** Only set for a quote captured across more than one photo/page (see docs/PLAN.md "여러 페이지 인용구"). */
    @ColumnInfo(name = "page_number_end") val pageNumberEnd: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
