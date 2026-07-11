package com.dyk1323.booklogs.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * TODO: full implementation per docs/PLAN.md "리마인더 알람/알림":
 *  1. Query READING books via AppContainer.bookRepository.
 *  2. PickReminderBookUseCase(readingBooks) -> pick one at random (or skip if empty).
 *  3. Build + show the notification via ReminderNotificationBuilder (not yet written), with a
 *     PendingIntent deep link into MainActivity opening that book's quick-action sheet.
 *  4. Reschedule tomorrow's alarm via ReminderScheduler.
 * Stubbed for now so the manifest reference is valid; wiring this up needs AlarmManager +
 * NotificationManager code that can't be verified without a real device.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Intentionally no-op until the full implementation above is wired in.
    }
}
