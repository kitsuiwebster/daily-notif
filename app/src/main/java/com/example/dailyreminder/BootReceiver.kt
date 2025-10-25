package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Only reschedule if notifications were enabled before reboot
            if (PreferenceManager.areNotificationsEnabled(context)) {
                AlarmScheduler.scheduleNext(context)
            }
        }
    }
}
