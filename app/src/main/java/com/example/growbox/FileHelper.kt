package com.example.growbox

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileHelper {
    fun saveData(context: Context, fileName: String, value: Double) {
        val file = File(context.filesDir, fileName)
        val writer = FileWriter(file, true)

        val timestamp = getCurrentTimestamp()
        writer.append("$timestamp,$value\n")
        writer.flush()
        writer.close()
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date()) // Pobranie aktualnej daty i godziny
    }
}