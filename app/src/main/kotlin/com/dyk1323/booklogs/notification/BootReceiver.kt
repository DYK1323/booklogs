package com.dyk1323.booklogs.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dyk1323.booklogs.BooklogsApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 재부팅 시 `AlarmManager` 알람이 사라지므로, 저장된 리마인더 설정을 다시 읽어 재등록. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val container = (context.applicationContext as BooklogsApplication).container

        CoroutineScope(Dispatchers.Main).launch {
            try {
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
