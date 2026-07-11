package com.dyk1323.booklogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.dyk1323.booklogs.ui.common.theme.BooklogsTheme
import com.dyk1323.booklogs.ui.dashboard.DashboardScreen
import com.dyk1323.booklogs.ui.dashboard.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val container = (application as BooklogsApplication).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                DashboardViewModel(container.bookRepository, container.readingLogRepository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooklogsTheme {
                DashboardScreen(viewModel = dashboardViewModel)
            }
        }
    }
}
