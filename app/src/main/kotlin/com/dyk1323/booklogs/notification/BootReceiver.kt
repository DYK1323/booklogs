package com.dyk1323.booklogs.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * TODO: on BOOT_COMPLETED, read AppSettingsDataStore's reminder time (if enabled) and re-register the
 * alarm via ReminderScheduler — AlarmManager alarms are cleared on reboot. Stubbed until
 * ReminderScheduler / AppSettingsDataStore are implemented.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Intentionally no-op until the full implementation above is wired in.
    }
}
