package com.dyk1323.booklogs.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.TimeZone

/**
 * docs/PLAN.md "리마인더 알람/알림" — 반복 알람 대신 다음 발생 시각 하나만 매번 재예약해
 * Doze 하 `setRepeating` 오차 누적을 피한다.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(hour: Int, minute: Int) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(hour, minute),
            pendingIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val REQUEST_CODE = 2001
    }
}

/** 오늘 그 시각이 이미 지났으면 내일로 넘김. 순수 함수로 분리해 Android 의존성 없이 단위테스트 가능. */
internal fun nextTriggerMillis(
    hour: Int,
    minute: Int,
    nowMillis: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
): Long {
    val trigger = Calendar.getInstance(zone).apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (trigger.timeInMillis <= nowMillis) {
        trigger.add(Calendar.DAY_OF_YEAR, 1)
    }
    return trigger.timeInMillis
}
