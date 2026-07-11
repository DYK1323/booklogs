package com.dyk1323.booklogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dyk1323.booklogs.ui.common.theme.BooklogsTheme
import com.dyk1323.booklogs.ui.dashboard.DashboardViewModel
import com.dyk1323.booklogs.ui.detail.BookDetailViewModel
import com.dyk1323.booklogs.ui.library.LibraryViewModel
import com.dyk1323.booklogs.ui.navigation.BooklogsNavHost
import com.dyk1323.booklogs.ui.quote.QuoteCaptureViewModel
import com.dyk1323.booklogs.ui.registration.BookRegistrationViewModel
import com.dyk1323.booklogs.ui.review.ReviewEditorViewModel

class MainActivity : ComponentActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooklogsTheme {
                BooklogsNavHost(
                    dashboardViewModel = dashboardViewModel,
                    registrationViewModel = registrationViewModel,
                    bookDetailViewModel = bookDetailViewModel,
                    libraryViewModel = libraryViewModel,
                    quoteCaptureViewModel = quoteCaptureViewModel,
                    reviewEditorViewModel = reviewEditorViewModel,
                )
            }
        }
    }
}
