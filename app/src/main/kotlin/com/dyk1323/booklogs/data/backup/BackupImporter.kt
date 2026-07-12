package com.dyk1323.booklogs.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.dyk1323.booklogs.data.local.BooklogsDatabase
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * docs/PLAN.md "백업/복원" — reads a [BackupEnvelope] JSON from the SAF `Uri` the user picked via
 * ACTION_OPEN_DOCUMENT and replaces the entire database with it (delete-all + re-insert-all in one
 * Room transaction, so a mid-way failure rolls back instead of leaving half-imported data). The UI is
 * responsible for the blocking "this replaces everything" confirmation before calling this — this
 * class does no confirmation of its own, matching [com.dyk1323.booklogs.domain.usecase.DeleteBookUseCase]'s
 * split of "usecase does the destructive thing, caller owns the confirm dialog."
 */
class BackupImporter(
    private val context: Context,
    private val database: BooklogsDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun importFrom(uri: Uri) = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open input stream for $uri")
        val content = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        val envelope = json.decodeFromString(BackupEnvelope.serializer(), content)

        database.withTransaction {
            // Children first so FK constraints never see an orphaned row mid-wipe, even though
            // onDelete=CASCADE from books would already cover most of this.
            database.quoteCommentDao().deleteAll()
            database.reviewDao().deleteAll()
            database.quoteDao().deleteAll()
            database.readingLogDao().deleteAll()
            database.readingRoundDao().deleteAll()
            database.bookDao().deleteAll()

            // Parents first on the way back in. Original ids are preserved (Room only autogenerates
            // when the provided id is 0), so bookId/readingRoundId(logs only)/quoteId references in the
            // JSON stay valid — reviews only reference bookId now (see docs/PLAN.md "라운드 이력 편집").
            envelope.books.forEach { database.bookDao().insert(it.toEntity()) }
            envelope.rounds.forEach { database.readingRoundDao().insert(it.toEntity()) }
            envelope.logs.forEach { database.readingLogDao().insert(it.toEntity()) }
            envelope.quotes.forEach { database.quoteDao().insert(it.toEntity()) }
            envelope.reviews.forEach { database.reviewDao().insert(it.toEntity()) }
            envelope.comments.forEach { database.quoteCommentDao().insert(it.toEntity()) }
        }
    }
}
