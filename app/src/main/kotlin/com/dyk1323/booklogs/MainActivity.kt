package com.dyk1323.booklogs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dyk1323.booklogs.data.settings.AppSettings
import com.dyk1323.booklogs.notification.ReminderNotificationBuilder
import com.dyk1323.booklogs.notification.ReminderScheduler
import com.dyk1323.booklogs.ui.bookedit.BookEditViewModel
import com.dyk1323.booklogs.ui.common.theme.BooklogsTheme
import com.dyk1323.booklogs.ui.dashboard.DashboardViewModel
import com.dyk1323.booklogs.ui.detail.BookDetailViewModel
import com.dyk1323.booklogs.ui.library.LibraryViewModel
import com.dyk1323.booklogs.ui.navigation.BooklogsNavHost
import com.dyk1323.booklogs.ui.quote.QuoteCaptureViewModel
import com.dyk1323.booklogs.ui.registration.BookRegistrationViewModel
import com.dyk1323.booklogs.ui.review.ReviewEditorViewModel
import com.dyk1323.booklogs.ui.settings.SettingsViewModel

class MainActivity : ComponentActivity() {

    private var pendingReminderBookId by mutableStateOf<Long?>(null)

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                DashboardViewModel(
                    bookRepository = container.bookRepository,
                    readingLogRepository = container.readingLogRepository,
                    readingRoundRepository = container.readingRoundRepository,
                    logProgressUseCase = container.logProgressUseCase,
                    editLogUseCase = container.editLogUseCase,
                    deleteLogUseCase = container.deleteLogUseCase,
                    appSettingsDataStore = container.appSettingsDataStore,
                ) as T
        }
    }

    private val registrationViewModel: BookRegistrationViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                BookRegistrationViewModel(
                    bookMetadataRepository = container.bookMetadataRepository,
                    bookRepository = container.bookRepository,
                    registerBookUseCase = container.registerBookUseCase,
                ) as T
        }
    }

    private val bookDetailViewModel: BookDetailViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                BookDetailViewModel(
                    bookRepository = container.bookRepository,
                    readingLogRepository = container.readingLogRepository,
                    quoteRepository = container.quoteRepository,
                    reviewRepository = container.reviewRepository,
                    changeBookStatusUseCase = container.changeBookStatusUseCase,
                    deleteBookUseCase = container.deleteBookUseCase,
                    deleteLogUseCase = container.deleteLogUseCase,
                    editLogUseCase = container.editLogUseCase,
                ) as T
        }
    }

    private val bookEditViewModel: BookEditViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                BookEditViewModel(
                    bookRepository = container.bookRepository,
                ) as T
        }
    }

    private val libraryViewModel: LibraryViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                LibraryViewModel(
                    bookRepository = container.bookRepository,
                    readingLogRepository = container.readingLogRepository,
                ) as T
        }
    }

    private val quoteCaptureViewModel: QuoteCaptureViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                QuoteCaptureViewModel(
                    quoteRepository = container.quoteRepository,
                ) as T
        }
    }

    private val reviewEditorViewModel: ReviewEditorViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ReviewEditorViewModel(
                    readingRoundRepository = container.readingRoundRepository,
                    reviewRepository = container.reviewRepository,
                ) as T
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                SettingsViewModel(
                    appSettingsDataStore = container.appSettingsDataStore,
                    reminderScheduler = ReminderScheduler(applicationContext),
                    backupExporter = container.backupExporter,
                    backupImporter = container.backupImporter,
                ) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingReminderBookId = extractReminderBookId(intent)
        val appSettingsDataStore = (application as BooklogsApplication).container.appSettingsDataStore
        setContent {
            val settings by appSettingsDataStore.settings.collectAsState(initial = AppSettings())
            BooklogsTheme(themeMode = settings.themeMode) {
                BooklogsNavHost(
                    dashboardViewModel = dashboardViewModel,
                    registrationViewModel = registrationViewModel,
                    bookDetailViewModel = bookDetailViewModel,
                    bookEditViewModel = bookEditViewModel,
                    libraryViewModel = libraryViewModel,
                    quoteCaptureViewModel = quoteCaptureViewModel,
                    reviewEditorViewModel = reviewEditorViewModel,
                    settingsViewModel = settingsViewModel,
                    pendingReminderBookId = pendingReminderBookId,
                    onPendingReminderBookIdConsumed = { pendingReminderBookId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractReminderBookId(intent)?.let { pendingReminderBookId = it }
    }

    private fun extractReminderBookId(intent: Intent?): Long? {
        val bookId = intent?.getLongExtra(ReminderNotificationBuilder.EXTRA_BOOK_ID, -1L) ?: -1L
        return bookId.takeIf { it > 0 }
    }
}
