package com.example.dailyreminder

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

object MessageRepository {
    private var cache: List<String>? = null

    fun randomMessage(context: Context): String {
        val allMessages = cache ?: load(context).also { cache = it }
        if (allMessages.isEmpty()) return "Coucou 👋"
        
        // Update total message count in preferences
        PreferenceManager.setTotalMessagesCount(context, allMessages.size)
        
        // Check if we need to reset the cycle
        if (PreferenceManager.shouldResetMessageCycle(context, allMessages.size)) {
            PreferenceManager.resetSentMessages(context)
            PreferenceManager.setTotalMessagesCount(context, allMessages.size)
        }
        
        // Get messages that haven't been sent yet
        val sentMessages = PreferenceManager.getSentMessages(context)
        val availableMessages = allMessages.filter { message -> message !in sentMessages }
        
        // If no available messages (shouldn't happen due to reset logic above), reset cycle
        if (availableMessages.isEmpty()) {
            PreferenceManager.resetSentMessages(context)
            return allMessages[Random.nextInt(allMessages.size)]
        }
        
        // Select random message from available (unsent) messages
        return availableMessages[Random.nextInt(availableMessages.size)]
    }
    
    fun markMessageAsSent(context: Context, message: String) {
        PreferenceManager.addSentMessage(context, message)
    }
    
    fun getAllMessages(context: Context): List<String> {
        return cache ?: load(context).also { cache = it }
    }
    
    fun getAvailableMessagesCount(context: Context): Int {
        val allMessages = getAllMessages(context)
        val sentMessages = PreferenceManager.getSentMessages(context)
        return allMessages.size - sentMessages.size
    }
    
    fun getSentMessagesCount(context: Context): Int {
        return PreferenceManager.getSentMessages(context).size
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
