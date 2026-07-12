package com.dyk1323.booklogs.data.backup

import android.content.Context
import android.net.Uri
import com.dyk1323.booklogs.data.local.BooklogsDatabase
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** docs/PLAN.md "백업/복원" — dumps all 6 tables to the SAF `Uri` the user picked via ACTION_CREATE_DOCUMENT. */
class BackupExporter(
    private val context: Context,
    private val database: BooklogsDatabase,
    private val json: Json = Json { prettyPrint = true },
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val envelope = BackupEnvelope(
            exportedAt = System.currentTimeMillis(),
            books = database.bookDao().getAll().map { it.toBackupDto() },
            rounds = database.readingRoundDao().getAll().map { it.toBackupDto() },
            logs = database.readingLogDao().getAll().map { it.toBackupDto() },
            quotes = database.quoteDao().getAll().map { it.toBackupDto() },
            reviews = database.reviewDao().getAll().map { it.toBackupDto() },
            comments = database.quoteCommentDao().getAll().map { it.toBackupDto() },
        )
        val content = json.encodeToString(BackupEnvelope.serializer(), envelope)
        val stream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open output stream for $uri")
        stream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
    }
}
