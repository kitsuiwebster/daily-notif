package com.example.dailyreminder

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

object HoursRepository {
    data class HoursConfig(val hours: List<Int>, val randomMinute: Boolean)

    private var cache: HoursConfig? = null

    fun randomHourAndMinute(context: Context): Pair<Int, Int> {
        val cfg = cache ?: load(context).also { cache = it }
        val hour = if (cfg.hours.isEmpty()) 10 else cfg.hours[Random.nextInt(cfg.hours.size)]
        val minute = if (cfg.randomMinute) Random.nextInt(0, 60) else 0
        return hour to minute
    }

    private fun load(context: Context): HoursConfig {
        return try {
            val input = context.resources.openRawResource(R.raw.hours)
            val json = BufferedReader(InputStreamReader(input)).use { it.readText() }
            val obj = JSONObject(json)
            val hoursJson = obj.optJSONArray("hours")
            val hours = mutableListOf<Int>()
            if (hoursJson != null) {
                for (i in 0 until hoursJson.length()) {
                    val v = hoursJson.optInt(i, -1)
                    if (v in 0..23) hours.add(v)
                }
            }
            val randomMinute = obj.optBoolean("random_minute", true)
            HoursConfig(hours.ifEmpty { listOf(10) }, randomMinute)
        } catch (e: Exception) {
            HoursConfig(listOf(10), true)
        }
    }
}
