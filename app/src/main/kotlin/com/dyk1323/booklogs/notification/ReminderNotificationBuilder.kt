package com.dyk1323.booklogs.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dyk1323.booklogs.MainActivity
import com.dyk1323.booklogs.domain.model.Book

/** docs/PLAN.md "리마인더 알람/알림" — 알림 콘텐츠 + 딥링크(bookId) PendingIntent 구성. */
object ReminderNotificationBuilder {
    const val EXTRA_BOOK_ID = "reminder_book_id"
    private const val CHANNEL_ID = "reading_reminder"
    private const val NOTIFICATION_ID = 1001

    fun show(context: Context, book: Book, progressPercent: Int?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        ensureChannel(notificationManager)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_BOOK_ID, book.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            book.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val contentText = if (progressPercent != null) {
            "『${book.title}』 $progressPercent% 읽는 중"
        } else {
            "『${book.title}』 읽는 중"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("오늘도 읽어볼까요?")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "독서 리마인더", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
}
