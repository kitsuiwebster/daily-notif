package com.example.dailyreminder

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId

object DateOverrideRepository {
    private var cache: Map<String, String>? = null

    fun messageForTodayOrNull(context: Context): String? {
        val map = cache ?: load(context).also { cache = it }
        val today = LocalDate.now(ZoneId.systemDefault()).toString() // yyyy-MM-dd
        return map[today]
    }

    private fun load(context: Context): Map<String, String> {
        return try {
            val input = context.resources.openRawResource(R.raw.overrides)
            val json = BufferedReader(InputStreamReader(input)).use { it.readText() }
            val obj = JSONObject(json)
            val out = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optString(k, "")
                if (k.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && v.isNotBlank()) {
                    out[k] = v
                }
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
