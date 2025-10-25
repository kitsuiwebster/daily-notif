package com.example.dailyreminder

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

object PreferenceManager {
    private const val PREFS_NAME = "daily_reminder_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_LAST_NOTIFICATION_DATE = "last_notification_date"
    private const val KEY_NEXT_SCHEDULED_TIME = "next_scheduled_time"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }
    
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }
    
    fun shouldSendNotificationToday(context: Context): Boolean {
        val today = getCurrentDateString()
        val lastNotificationDate = getPrefs(context).getString(KEY_LAST_NOTIFICATION_DATE, "")
        return lastNotificationDate != today
    }
    
    fun markNotificationSentToday(context: Context) {
        val today = getCurrentDateString()
        getPrefs(context).edit()
            .putString(KEY_LAST_NOTIFICATION_DATE, today)
            .apply()
    }
    
    fun setNextScheduledTime(context: Context, timeInMillis: Long) {
        getPrefs(context).edit()
            .putLong(KEY_NEXT_SCHEDULED_TIME, timeInMillis)
            .apply()
    }
    
    fun getNextScheduledTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_NEXT_SCHEDULED_TIME, 0L)
    }
    
    private fun getCurrentDateString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)+1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
    
    fun hasAlarmAlreadyScheduledForToday(context: Context): Boolean {
        val nextScheduledTime = getNextScheduledTime(context)
        if (nextScheduledTime == 0L) return false
        
        val now = System.currentTimeMillis()
        // If we have a scheduled time in the future, consider it valid
        return nextScheduledTime > now
    }
}