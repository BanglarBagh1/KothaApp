package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

object DiagnosticsLog {

    enum class Category {
        SYSTEM,
        RECOGNIZER,
        ACCESSIBILITY,
        REPAIR,
        INSERTION,
        ERROR
    }

    data class Entry(
        val id: Long,
        val timestamp: Long,
        val category: Category,
        val message: String
    ) {
        val formattedTime: String
            get() = timeFormat.format(Date(timestamp))

        companion object {
            private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        }
    }

    private const val MAX_ENTRIES = 500
    private var sequenceId = 0L
    private val deque = ConcurrentLinkedDeque<Entry>()

    private val _entriesFlow = MutableStateFlow<List<Entry>>(emptyList())
    val entriesFlow: StateFlow<List<Entry>> = _entriesFlow.asStateFlow()

    @Synchronized
    fun log(category: Category, message: String) {
        val entry = Entry(
            id = ++sequenceId,
            timestamp = System.currentTimeMillis(),
            category = category,
            message = message
        )
        deque.addLast(entry)
        while (deque.size > MAX_ENTRIES) {
            deque.pollFirst()
        }
        _entriesFlow.value = deque.toList().reversed()
    }

    fun clear() {
        deque.clear()
        _entriesFlow.value = emptyList()
    }

    fun getExportText(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        val sb = StringBuilder()
        sb.append("=== Kotha Diagnostics Log ===\n")
        sb.append("Exported at: ").append(sdf.format(Date())).append("\n\n")
        deque.forEach { entry ->
            sb.append("[")
                .append(sdf.format(Date(entry.timestamp)))
                .append("] [")
                .append(entry.category.name)
                .append("] ")
                .append(entry.message)
                .append("\n")
        }
        return sb.toString()
    }
}
