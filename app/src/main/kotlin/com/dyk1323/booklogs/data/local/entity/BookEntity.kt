package com.dyk1323.booklogs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books", indices = [Index(value = ["isbn"])])
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isbn: String?,
    val title: String,
    val author: String?,
    val publisher: String?,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String?,
    @ColumnInfo(name = "total_pages") val totalPages: Int?,
    /** Serialized [com.dyk1323.booklogs.domain.model.BookStatus] name (READING/PAUSED/FINISHED/DROPPED/PLANNED). */
    val status: String,
    /** Serialized [com.dyk1323.booklogs.domain.model.BookFormat] name (PHYSICAL/EBOOK). */
    val format: String,
    val genre: String?,
    val country: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
