package com.dyk1323.booklogs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dyk1323.booklogs.ui.dashboard.DashboardScreen
import com.dyk1323.booklogs.ui.dashboard.DashboardViewModel
import com.dyk1323.booklogs.ui.detail.BookDetailScreen
import com.dyk1323.booklogs.ui.detail.BookDetailViewModel
import com.dyk1323.booklogs.ui.library.LibraryScreen
import com.dyk1323.booklogs.ui.library.LibraryViewModel
import com.dyk1323.booklogs.ui.quote.QuoteCaptureScreen
import com.dyk1323.booklogs.ui.quote.QuoteCaptureViewModel
import com.dyk1323.booklogs.ui.registration.BarcodeScanScreen
import com.dyk1323.booklogs.ui.registration.BookConfirmFormScreen
import com.dyk1323.booklogs.ui.registration.BookRegistrationScreen
import com.dyk1323.booklogs.ui.registration.BookRegistrationViewModel
import com.dyk1323.booklogs.ui.registration.TitleSearchScreen

@Composable
fun BooklogsNavHost(
    dashboardViewModel: DashboardViewModel,
    registrationViewModel: BookRegistrationViewModel,
    bookDetailViewModel: BookDetailViewModel,
    libraryViewModel: LibraryViewModel,
    quoteCaptureViewModel: QuoteCaptureViewModel,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Destinations.DASHBOARD) {
        composable(Destinations.DASHBOARD) {
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
            )
        }
    }
}
