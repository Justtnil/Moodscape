package com.example.moodscape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodscape.ui.screens.HomeScreen
import com.example.moodscape.ui.screens.InsightsScreen
import com.example.moodscape.ui.screens.LogbookScreen
import com.example.moodscape.ui.theme.MoodscapeTheme
import com.example.moodscape.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = MainViewModel.MainViewModelFactory(application)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            MoodscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLogbook = {
                    navController.navigate("logbook")
                }
            )
        }
        composable("logbook") {
            LogbookScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToInsights = {
                    navController.navigate("insights")
                }
            )
        }
        composable("insights") {
            InsightsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}