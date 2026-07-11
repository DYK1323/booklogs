package com.dyk1323.booklogs.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dyk1323.booklogs.BooklogsApplication
import com.dyk1323.booklogs.domain.model.BookStatus
import com.dyk1323.booklogs.domain.usecase.computeBookProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * docs/PLAN.md "리마인더 알람/알림" — 알람 발화 시 (1) READING 책 중 무작위 1권 선택 (2) 알림 표시
 * (3) 다음날 같은 시각으로 알람 재예약.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val container = (context.applicationContext as BooklogsApplication).container

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val books = container.bookRepository.observeAll().first()
                val readingBooks = books.filter { it.status == BookStatus.READING }
                val picked = container.pickReminderBookUseCase(readingBooks)
                if (picked != null) {
                    val logs = container.readingLogRepository.observeAll().first()
                    val currentPage = logs
                        .filter { it.bookId == picked.id }
                        .maxByOrNull { it.loggedAt }
                        ?.currentPage
                    val progressPercent = computeBookProgress(currentPage, picked.totalPages)
                        ?.let { (it * 100).toInt() }
                    ReminderNotificationBuilder.show(context.applicationContext, picked, progressPercent)
                }

                val settings = container.appSettingsDataStore.settings.first()
                if (settings.reminderEnabled) {
                    ReminderScheduler(context.applicationContext)
                        .schedule(settings.reminderHour, settings.reminderMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
