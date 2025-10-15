package com.example.dailyreminder

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

object MessageRepository {
    private var cache: List<String>? = null

    fun randomMessage(context: Context): String {
        val list = cache ?: load(context).also { cache = it }
        if (list.isEmpty()) return "Coucou 👋"
        return list[Random.nextInt(list.size)]
    }

    private fun load(context: Context): List<String> {
        return try {
            val input = context.resources.openRawResource(R.raw.notifications)
            val json = BufferedReader(InputStreamReader(input)).use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                arr.optString(i, null)
            }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            listOf("Coucou 👋")
        }
    }
}
