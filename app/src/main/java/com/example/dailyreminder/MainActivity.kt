package com.example.dailyreminder

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) askExactAlarmThenStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 24)
        }
        val title = TextView(this).apply {
            text = "Daily Reminder\n\nActive la notif quotidienne, ou teste tout de suite."
            textSize = 18f
        }
        val enableBtn = Button(this).apply { text = "Activer les rappels (1/jour)" }
        val testNowBtn = Button(this).apply { text = "Tester maintenant (notif immédiate)" }
        val test30sBtn = Button(this).apply { text = "Planifier dans 30 secondes" }
        status = TextView(this)

        root.addView(title)
        root.addView(enableBtn)
        root.addView(testNowBtn)
        root.addView(test30sBtn)
        root.addView(status)
        setContentView(root)

        enableBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                askExactAlarmThenStart()
            }
        }

        testNowBtn.setOnClickListener {
            // Affiche une notification tout de suite (utile pour debug)
            NotificationHelper.showNow(this)
        }

        test30sBtn.setOnClickListener {
            scheduleInSeconds(30)
            status.text = "Test : une notif devrait arriver dans ~30s."
        }
    }

    private fun askExactAlarmThenStart() {
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                })
                status.text = "Autorise les alarmes exactes puis reclique."
                return
            }
        }
        AlarmScheduler.scheduleNext(this)
        status.text = "OK : 1 notif/jour (heure aléatoire)."
    }

    private fun scheduleInSeconds(sec: Int) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + sec * 1000L
        val pi = PendingIntent.getBroadcast(
            this, 2002, Intent(this, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
