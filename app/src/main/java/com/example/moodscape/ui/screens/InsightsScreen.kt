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
import com.example.moodscape.data.MoodEntryWithCategory
import com.example.moodscape.viewmodel.MainViewModel

// Placeholder for the complex insight object
data class LocalInsight(
    val trendMessage: String,
    val dayOfWeekMessage: String,
    val keywordMessage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val allMoods by viewModel.allEntries.collectAsState()
    var insight by remember { mutableStateOf<LocalInsight?>(null) }

    // Calculate insights when the screen is first composed
    LaunchedEffect(allMoods) {
        if (allMoods.isNotEmpty()) {
            // This is a simplified calculation. A real implementation would be more complex.
            insight = calculateLocalInsights(allMoods)
        }
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
                InsightCard("Weekly Trend", insight?.trendMessage ?: "Not enough data yet.")
            }
            item {
                InsightCard("Day of the Week", insight?.dayOfWeekMessage ?: "Not enough data yet.")
            }
            item {
                InsightCard("Note Keywords", insight?.keywordMessage ?: "Not enough data yet.")
            }
        }
    }
}

@Composable
fun InsightCard(title: String, message: String) {
    Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Simplified placeholder for the insight calculation logic
fun calculateLocalInsights(entries: List<MoodEntryWithCategory>): LocalInsight {
    // A full implementation of the rules would go here.
    // This is a placeholder to show the UI.
    val trend = "Your mood has been stable this week."
    val dayOfWeek = "You seem to be happiest on Saturdays."
    val keyword = "'Work' appears in 60% of your less positive entries."
    return LocalInsight(trend, dayOfWeek, keyword)
}