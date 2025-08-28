package com.example.moodscape.viewmodel

import android.app.Application
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moodscape.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val moodEntryDao: MoodEntryDao
    private val moodCategoryDao: MoodCategoryDao

    val allEntries: StateFlow<List<MoodEntryWithCategory>>
    val allCategories: StateFlow<List<MoodCategory>>

    init {
        val database = AppDatabase.getDatabase(application)
        moodEntryDao = database.moodEntryDao()
        moodCategoryDao = database.moodCategoryDao()

        allEntries = moodEntryDao.getAllEntriesWithCategory().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = moodCategoryDao.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun saveMoodEntry(entry: MoodEntry) = viewModelScope.launch {
        moodEntryDao.insertMoodEntry(entry)
    }

    fun addCategory(name: String, colorHex: String) = viewModelScope.launch {
        val newCategory = MoodCategory(name = name, colorHex = colorHex)
        moodCategoryDao.insertCategory(newCategory)
    }

    fun deleteCategory(category: MoodCategory) = viewModelScope.launch {
        moodCategoryDao.deleteCategory(category)
    }

    fun processVoiceNote(spokenText: String): String {
        return spokenText
    }

    // --- UPDATED PDF EXPORT FUNCTION ---
    // This function now generates and returns a PdfDocument
    fun generatePdf(entries: List<MoodEntryWithCategory>): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 40f
        val xPosition = 20f

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Moodscape LogBook", xPosition, yPosition, paint)
        yPosition += 40f

        paint.textSize = 12f
        paint.isFakeBoldText = false

        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

        entries.forEach { entry ->
            // Check if we need a new page
            if (yPosition > 750) { // Leave some margin at bottom
                pdfDocument.finishPage(page)
                // Create new page
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = 40f // Reset y position for new page
            }

            paint.isFakeBoldText = true
            canvas.drawText(dateFormat.format(Date(entry.timestamp)), xPosition, yPosition, paint)
            yPosition += 20

            paint.isFakeBoldText = false
            // Get actual mood name
            val moodOption = defaultMoodOptions.find { it.emoji == entry.emoji }
            val moodName = moodOption?.name ?: "Custom Mood"
            canvas.drawText("• Mood: ${entry.emoji} ($moodName)", xPosition, yPosition, paint)
            yPosition += 15

            entry.categoryName?.let {
                canvas.drawText("• Category: $it", xPosition, yPosition, paint)
                yPosition += 15
            }

            if (entry.note.isNotBlank()) {
                // Handle long notes that might need wrapping
                val noteLines = entry.note.split(" ")
                var currentLine = "• Note: "

                for (word in noteLines) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    // Simple word wrapping - you might want to improve this
                    if (currentLine.isNotEmpty() && testLine.length > 50) {
                        canvas.drawText(currentLine, xPosition, yPosition, paint)
                        yPosition += 15
                        currentLine = word

                        // Check for new page again
                        if (yPosition > 750) {
                            pdfDocument.finishPage(page)
                            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            yPosition = 40f
                        }
                    } else {
                        currentLine = testLine
                    }
                }

                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, xPosition, yPosition, paint)
                    yPosition += 15
                }
            }
            yPosition += 20
        }

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}