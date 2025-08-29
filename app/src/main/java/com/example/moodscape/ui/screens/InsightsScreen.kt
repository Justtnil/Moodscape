package com.example.moodscape.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moodscape.data.MoodInsights
import com.example.moodscape.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val allMoods by viewModel.allEntries.collectAsState()
    var insights by remember { mutableStateOf<MoodInsights?>(null) }

    // Calculate insights when the screen is first composed or when data changes
    LaunchedEffect(allMoods) {
        insights = viewModel.generateMoodInsights()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Mood Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InsightCard(
                    title = "Weekly Trend",
                    message = insights?.trendMessage ?: "Analyzing your mood trends..."
                )
            }
            item {
                InsightCard(
                    title = "Day of the Week",
                    message = insights?.dayOfWeekMessage ?: "Discovering your weekly patterns..."
                )
            }
            item {
                InsightCard(
                    title = "Note Keywords",
                    message = insights?.keywordMessage ?: "Analyzing keywords in your notes..."
                )
            }
        }
    }
}

@Composable
fun InsightCard(title: String, message: String) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}