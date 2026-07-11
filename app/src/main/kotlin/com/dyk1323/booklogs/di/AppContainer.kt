package com.dyk1323.booklogs.di

import android.content.Context
import androidx.room.Room
import com.dyk1323.booklogs.BuildConfig
import com.dyk1323.booklogs.data.local.BooklogsDatabase
import com.dyk1323.booklogs.data.settings.AppSettingsDataStore
import com.dyk1323.booklogs.data.remote.GoogleBooksApi
import com.dyk1323.booklogs.data.remote.KakaoBooksApi
import com.dyk1323.booklogs.data.repository.BookMetadataRepositoryImpl
import com.dyk1323.booklogs.data.repository.BookRepositoryImpl
import com.dyk1323.booklogs.data.repository.QuoteRepositoryImpl
import com.dyk1323.booklogs.data.repository.ReadingLogRepositoryImpl
import com.dyk1323.booklogs.data.repository.ReadingRoundRepositoryImpl
import com.dyk1323.booklogs.data.repository.ReviewRepositoryImpl
import com.dyk1323.booklogs.data.repository.RoomTransactionRunner
import com.dyk1323.booklogs.domain.repository.BookMetadataRepository
import com.dyk1323.booklogs.domain.repository.BookRepository
import com.dyk1323.booklogs.domain.repository.QuoteRepository
import com.dyk1323.booklogs.domain.repository.ReadingLogRepository
import com.dyk1323.booklogs.domain.repository.ReadingRoundRepository
import com.dyk1323.booklogs.domain.repository.ReviewRepository
import com.dyk1323.booklogs.domain.repository.TransactionRunner
import com.dyk1323.booklogs.domain.usecase.ChangeBookStatusUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteBookUseCase
import com.dyk1323.booklogs.domain.usecase.DeleteLogUseCase
import com.dyk1323.booklogs.domain.usecase.EditLogUseCase
import com.dyk1323.booklogs.domain.usecase.LogProgressUseCase
import com.dyk1323.booklogs.domain.usecase.PickReminderBookUseCase
import com.dyk1323.booklogs.domain.usecase.RegisterBookUseCase
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Manual DI container (no Hilt — see docs/PLAN.md "DI" row: a handful of repositories doesn't
 * justify KSP annotation-processing overhead). Everything is lazily constructed once per process.
 */
class AppContainer(context: Context) {

    private val database: BooklogsDatabase = Room.databaseBuilder(
        context.applicationContext,
        BooklogsDatabase::class.java,
        BooklogsDatabase.DATABASE_NAME,
    ).build()

    val transactionRunner: TransactionRunner = RoomTransactionRunner(database)

    val bookRepository: BookRepository = BookRepositoryImpl(database.bookDao())
    val readingRoundRepository: ReadingRoundRepository = ReadingRoundRepositoryImpl(database.readingRoundDao())
    val readingLogRepository: ReadingLogRepository = ReadingLogRepositoryImpl(database.readingLogDao())
    val quoteRepository: QuoteRepository = QuoteRepositoryImpl(database.quoteDao())
    val reviewRepository: ReviewRepository = ReviewRepositoryImpl(database.reviewDao())

    val appSettingsDataStore = AppSettingsDataStore(context)

    private val json = Json { ignoreUnknownKeys = true }

    private val sharedHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // BODY so the raw Kakao/Google Books JSON is visible in logcat (filter tag "OkHttp")
                // while debugging metadata lookup issues — e.g. totalPages coming back empty even
                // though the book has page-count data on Google Books' own site (see docs/PLAN.md
                // "메타데이터 연동"). Authorization is redacted so the Kakao REST key never lands in
                // logs even at this level.
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            },
        )
        .build()

    private val kakaoBooksApi = KakaoBooksApi(sharedHttpClient, BuildConfig.KAKAO_API_KEY, json)
    private val googleBooksApi = GoogleBooksApi(sharedHttpClient, json)

    val bookMetadataRepository: BookMetadataRepository = BookMetadataRepositoryImpl(kakaoBooksApi, googleBooksApi)

    val logProgressUseCase = LogProgressUseCase(readingLogRepository)
    val editLogUseCase = EditLogUseCase(readingLogRepository)
    val deleteLogUseCase = DeleteLogUseCase(readingLogRepository)
    val changeBookStatusUseCase = ChangeBookStatusUseCase(bookRepository, readingRoundRepository, transactionRunner)
    val deleteBookUseCase = DeleteBookUseCase(bookRepository)
    val registerBookUseCase = RegisterBookUseCase(bookRepository, changeBookStatusUseCase)
    val pickReminderBookUseCase = PickReminderBookUseCase()

    // AggregateDailyPagesUseCase, AggregateBooksByAttributeUseCase, ComputeBookProgressUseCase,
    // ConvertPagePercentUseCase are plain top-level functions (see :domain/usecase) — no instance needed.
}
