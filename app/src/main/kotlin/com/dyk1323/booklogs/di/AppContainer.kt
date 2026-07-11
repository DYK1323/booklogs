package com.dyk1323.booklogs.di

import android.content.Context
import androidx.room.Room
import com.dyk1323.booklogs.BuildConfig
import com.dyk1323.booklogs.data.local.BooklogsDatabase
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

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

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonConverterFactory = json.asConverterFactory("application/json".toMediaType())

    // Kakao requires an "Authorization: KakaoAK {key}" header on every request; Google Books doesn't,
    // so it gets its own plain client instead of a header that would be silently wrong on that host.
    private val kakaoHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
                    .build(),
            )
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val googleHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val kakaoBooksApi: KakaoBooksApi = Retrofit.Builder()
        .baseUrl("https://dapi.kakao.com/")
        .client(kakaoHttpClient)
        .addConverterFactory(jsonConverterFactory)
        .build()
        .create(KakaoBooksApi::class.java)

    private val googleBooksApi: GoogleBooksApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/")
        .client(googleHttpClient)
        .addConverterFactory(jsonConverterFactory)
        .build()
        .create(GoogleBooksApi::class.java)

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
