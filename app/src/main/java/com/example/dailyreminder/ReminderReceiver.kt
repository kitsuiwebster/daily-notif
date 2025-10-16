package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Si c'est un test avec message personnalisé
        val testMessage = intent?.getStringExtra("test_message")
        if (testMessage != null) {
            NotificationHelper.showNow(context, testMessage)
            return
        }
        
        // Si une phrase est définie pour AUJOURD'HUI (overrides.json), on l'utilise
        val override = DateOverrideRepository.messageForTodayOrNull(context)
        if (override != null) {
            NotificationHelper.showNow(context, override)
        } else {
            NotificationHelper.showNow(context)
        }
        // Replanifier la prochaine notification (app fonctionne à l'infini)
        AlarmScheduler.scheduleNext(context)
    }
}
