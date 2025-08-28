package com.example.moodscape.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodscape.data.MoodEntryWithCategory
import com.example.moodscape.data.defaultMoodOptions
import com.example.moodscape.utils.startOfDay
import com.example.moodscape.viewmodel.MainViewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogbookScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToInsights: () -> Unit
) {
    val context = LocalContext.current
    val allEntries by viewModel.allEntries.collectAsState()

    val filterOptions = listOf("All Time", "Last 7 Days", "Last 14 Days", "Last 30 Days", "Last 90 Days")
    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    val filteredMoods = remember(allEntries, selectedFilter) {
        if (selectedFilter == "All Time") {
            allEntries
        } else {
            val daysToSubtract = when (selectedFilter) {
                "Last 7 Days" -> 7
                "Last 14 Days" -> 14
                "Last 30 Days" -> 30
                "Last 90 Days" -> 90
                else -> Int.MAX_VALUE
            }
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
            val startDate = startOfDay(calendar.time).time

            allEntries.filter { it.timestamp >= startDate }
        }
    }

    // --- PERMISSION AND FILE SAVING LOGIC ---
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let { fileUri ->
                try {
                    val pdfDocument = viewModel.generatePdf(filteredMoods)
                    context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()
                    Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // If permission is granted, launch the file saver
                val fileName = "Moodscape_Export_${System.currentTimeMillis()}.pdf"
                pdfLauncher.launch(fileName)
            } else {
                Toast.makeText(context, "Permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show()
            }
        }
    )
    // --- END OF PERMISSION LOGIC ---


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood LogBook") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToInsights) {
                    Text("Analysis")
                }
                TextButton(onClick = {
                    // --- UPDATED ONCLICK LOGIC ---
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        // For older Android, request permission first
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        // For modern Android, just launch the file saver
                        val fileName = "Moodscape_Export_${System.currentTimeMillis()}.pdf"
                        pdfLauncher.launch(fileName)
                    }
                }) {
                    Text("Export PDF")
                }
                Box {
                    TextButton(onClick = { isFilterMenuExpanded = true }) {
                        Text(selectedFilter)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Open filter menu")
                    }
                    DropdownMenu(
                        expanded = isFilterMenuExpanded,
                        onDismissRequest = { isFilterMenuExpanded = false }
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFilter = option
                                    isFilterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (filteredMoods.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries for this period.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMoods) { mood ->
                        MoodLogItem(mood = mood)
                    }
                }
            }
        }
    }
}

@Composable
fun MoodLogItem(mood: MoodEntryWithCategory) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = dateFormat.format(Date(mood.timestamp)), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mood.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val moodName = defaultMoodOptions.find { it.emoji == mood.emoji }?.name ?: "Custom"
                    Text(moodName, fontWeight = FontWeight.SemiBold)
                    mood.categoryName?.let { categoryName ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(mood.categoryColorHex ?: "#808080")))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (!mood.note.isBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mood.note)
            }
        }
    }
}