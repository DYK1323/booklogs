package com.dyk1323.booklogs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dyk1323.booklogs.ui.bookedit.BookEditScreen
import com.dyk1323.booklogs.ui.bookedit.BookEditViewModel
import com.dyk1323.booklogs.ui.dashboard.DashboardScreen
import com.dyk1323.booklogs.ui.dashboard.DashboardViewModel
import com.dyk1323.booklogs.ui.detail.BookDetailScreen
import com.dyk1323.booklogs.ui.detail.BookDetailViewModel
import com.dyk1323.booklogs.ui.detail.LogListScreen
import com.dyk1323.booklogs.ui.detail.QuoteEditScreen
import com.dyk1323.booklogs.ui.detail.QuoteListScreen
import com.dyk1323.booklogs.ui.detail.RoundListScreen
import com.dyk1323.booklogs.ui.detail.ReviewListScreen
import com.dyk1323.booklogs.ui.library.LibraryScreen
import com.dyk1323.booklogs.ui.library.LibraryViewModel
import com.dyk1323.booklogs.ui.quote.QuoteCaptureScreen
import com.dyk1323.booklogs.ui.quote.QuoteCaptureViewModel
import com.dyk1323.booklogs.ui.registration.BarcodeScanScreen
import com.dyk1323.booklogs.ui.registration.BookConfirmFormScreen
import com.dyk1323.booklogs.ui.registration.BookRegistrationScreen
import com.dyk1323.booklogs.ui.registration.BookRegistrationViewModel
import com.dyk1323.booklogs.ui.registration.TitleSearchScreen
import com.dyk1323.booklogs.ui.review.ReviewEditorScreen
import com.dyk1323.booklogs.ui.review.ReviewEditorViewModel
import com.dyk1323.booklogs.ui.settings.SettingsScreen
import com.dyk1323.booklogs.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.first

@Composable
fun BooklogsNavHost(
    dashboardViewModel: DashboardViewModel,
    registrationViewModel: BookRegistrationViewModel,
    bookDetailViewModel: BookDetailViewModel,
    bookEditViewModel: BookEditViewModel,
    libraryViewModel: LibraryViewModel,
    quoteCaptureViewModel: QuoteCaptureViewModel,
    reviewEditorViewModel: ReviewEditorViewModel,
    settingsViewModel: SettingsViewModel,
    pendingReminderBookId: Long? = null,
    onPendingReminderBookIdConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Destinations.DASHBOARD) {
        composable(Destinations.DASHBOARD) {
            LaunchedEffect(pendingReminderBookId) {
                val bookId = pendingReminderBookId ?: return@LaunchedEffect
                dashboardViewModel.uiState.first { !it.isLoading }
                dashboardViewModel.openQuickLog(bookId)
                onPendingReminderBookIdConsumed()
            }
            DashboardScreen(
                viewModel = dashboardViewModel,
                onRegisterBookClick = {
                    registrationViewModel.reset()
                    navController.navigate(Destinations.REGISTRATION_ENTRY)
                },
                onBookDetailClick = { bookId ->
                    navController.navigate(Destinations.bookDetail(bookId))
                },
                onCaptureQuoteClick = { bookId ->
                    navController.navigate(Destinations.quoteCapture(bookId))
                },
                onLibraryClick = {
                    navController.navigate(Destinations.LIBRARY)
                },
                onSettingsClick = {
                    navController.navigate(Destinations.SETTINGS)
                },
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.LIBRARY) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onBookClick = { bookId -> navController.navigate(Destinations.bookDetail(bookId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.BOOK_DETAIL,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack()
                },
                onCaptureQuoteClick = {
                    navController.navigate(Destinations.quoteCapture(bookId))
                },
                onEditQuoteClick = { quoteId ->
                    navController.navigate(Destinations.quoteEditor(bookId, quoteId))
                },
                onViewAllRoundsClick = {
                    navController.navigate(Destinations.roundList(bookId))
                },
                onViewAllLogsClick = {
                    navController.navigate(Destinations.logList(bookId))
                },
                onViewAllQuotesClick = {
                    navController.navigate(Destinations.quoteList(bookId))
                },
                onWriteReviewClick = {
                    navController.navigate(Destinations.reviewEditor(bookId))
                },
                onEditReviewClick = { reviewId ->
                    navController.navigate(Destinations.reviewEditor(bookId, reviewId))
                },
                onViewAllReviewsClick = {
                    navController.navigate(Destinations.reviewList(bookId))
                },
                onEditClick = {
                    navController.navigate(Destinations.bookEdit(bookId))
                },
            )
        }

        composable(
            route = Destinations.ROUND_LIST,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) {
            RoundListScreen(
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.LOG_LIST,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) {
            LogListScreen(
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.QUOTE_LIST,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            QuoteListScreen(
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
                onCaptureQuoteClick = {
                    navController.navigate(Destinations.quoteCapture(bookId))
                },
                onEditQuoteClick = { quoteId ->
                    navController.navigate(Destinations.quoteEditor(bookId, quoteId))
                },
            )
        }

        composable(
            route = Destinations.QUOTE_EDITOR,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("quoteId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: return@composable
            QuoteEditScreen(
                bookId = bookId,
                quoteId = quoteId,
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.REVIEW_LIST,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            ReviewListScreen(
                viewModel = bookDetailViewModel,
                onBack = { navController.popBackStack() },
                onWriteReviewClick = {
                    navController.navigate(Destinations.reviewEditor(bookId))
                },
                onEditReviewClick = { reviewId ->
                    navController.navigate(Destinations.reviewEditor(bookId, reviewId))
                },
            )
        }

        composable(
            route = Destinations.BOOK_EDIT,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            BookEditScreen(
                bookId = bookId,
                viewModel = bookEditViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.QUOTE_CAPTURE,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            QuoteCaptureScreen(
                bookId = bookId,
                viewModel = quoteCaptureViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.REVIEW_EDITOR,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("reviewId") {
                    type = NavType.LongType
                    defaultValue = Destinations.NO_REVIEW_ID
                },
            ),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            val reviewId = backStackEntry.arguments?.getLong("reviewId")
                ?.takeIf { it != Destinations.NO_REVIEW_ID }
            ReviewEditorScreen(
                bookId = bookId,
                reviewId = reviewId,
                viewModel = reviewEditorViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.REGISTRATION_ENTRY) {
            BookRegistrationScreen(
                onScanBarcode = { navController.navigate(Destinations.REGISTRATION_SCAN) },
                onSearchByTitle = { navController.navigate(Destinations.REGISTRATION_SEARCH) },
                onManualEntry = {
                    registrationViewModel.startManualEntry()
                    navController.navigate(Destinations.REGISTRATION_CONFIRM)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.REGISTRATION_SCAN) {
            BarcodeScanScreen(
                onIsbnScanned = { isbn ->
                    registrationViewModel.lookupByIsbn(isbn)
                    navController.navigate(Destinations.REGISTRATION_CONFIRM)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.REGISTRATION_SEARCH) {
            val searchState by registrationViewModel.searchState.collectAsState()
            TitleSearchScreen(
                searchState = searchState,
                onQueryChanged = registrationViewModel::searchByTitle,
                onResultSelected = { candidate ->
                    registrationViewModel.selectSearchResult(candidate)
                    navController.navigate(Destinations.REGISTRATION_CONFIRM)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.REGISTRATION_CONFIRM) {
            val formState by registrationViewModel.formState.collectAsState()
            val lookupState by registrationViewModel.lookupState.collectAsState()
            val saveState by registrationViewModel.saveState.collectAsState()
            BookConfirmFormScreen(
                formState = formState,
                lookupState = lookupState,
                saveState = saveState,
                onRetryLookup = { formState.isbn?.let(registrationViewModel::lookupByIsbn) },
                onTitleChanged = registrationViewModel::updateTitle,
                onAuthorChanged = registrationViewModel::updateAuthor,
                onPublisherChanged = registrationViewModel::updatePublisher,
                onTotalPagesChanged = registrationViewModel::updateTotalPagesText,
                onGenreChanged = registrationViewModel::updateGenre,
                onCountryChanged = registrationViewModel::updateCountry,
                onFormatChanged = registrationViewModel::updateFormat,
                onStartReadingImmediatelyChanged = registrationViewModel::updateStartReadingImmediately,
                onSave = registrationViewModel::save,
                onSaved = {
                    navController.popBackStack(Destinations.DASHBOARD, inclusive = false)
                },
                onBack = { navController.popBackStack() },
                onGoToDuplicateBook = { bookId ->
                    navController.popBackStack(Destinations.DASHBOARD, inclusive = false)
                    navController.navigate(Destinations.bookDetail(bookId))
                },
            )
        }
    }
}
