package com.example.moodscape

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodscape.ui.components.NotificationPermissionDialog
import com.example.moodscape.ui.screens.HomeScreen
import com.example.moodscape.ui.screens.InsightsScreen
import com.example.moodscape.ui.screens.LogbookScreen
import com.example.moodscape.ui.theme.MoodscapeTheme
import com.example.moodscape.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    // In the permission launcher callback, update this part:
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - save preference and schedule worker
            sharedPreferences.edit()
                .putBoolean("notification_permission_asked", true)
                .putBoolean("notification_permission_granted", true)
                .putLong("last_permission_prompt_time", System.currentTimeMillis())
                .apply()

            // Schedule the mood reminder worker
            (application as? MoodscapeApplication)?.scheduleMoodReminder()
        } else {
            // Permission denied - save preference
            sharedPreferences.edit()
                .putBoolean("notification_permission_asked", true)
                .putBoolean("notification_permission_granted", false)
                .putLong("last_permission_prompt_time", System.currentTimeMillis())
                .apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("moodscape_prefs", MODE_PRIVATE)

        val viewModelFactory = MainViewModel.MainViewModelFactory(application)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            MoodscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppWithPermissionCheck(viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    fun AppWithPermissionCheck(viewModel: MainViewModel) {
        var showPermissionDialog by remember { mutableStateOf(false) }

        // Check if we should show the permission dialog
        LaunchedEffect(Unit) {
            showPermissionDialog = shouldShowPermissionDialog()
        }

        // Show the permission dialog if needed
        if (showPermissionDialog) {
            NotificationPermissionDialog(
                onAllow = {
                    showPermissionDialog = false
                    requestNotificationPermission()
                },
                onDeny = {
                    showPermissionDialog = false
                    // Save that user denied but we should ask again in 48 hours
                    sharedPreferences.edit()
                        .putBoolean("notification_permission_asked", true)
                        .putBoolean("notification_permission_granted", false)
                        .putLong("last_permission_prompt_time", System.currentTimeMillis())
                        .apply()
                }
            )
        }

        AppNavigation(viewModel = viewModel)
    }

    // Add notification permission request function
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted - save preference and schedule worker
                    sharedPreferences.edit()
                        .putBoolean("notification_permission_asked", true)
                        .putBoolean("notification_permission_granted", true)
                        .apply()

                    // Schedule the mood reminder worker
                    (application as? MoodscapeApplication)?.scheduleMoodReminder()
                }
                else -> {
                    // Request the permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, notification permission is granted by default
            sharedPreferences.edit()
                .putBoolean("notification_permission_asked", true)
                .putBoolean("notification_permission_granted", true)
                .apply()

            // Schedule the mood reminder worker
            (application as? MoodscapeApplication)?.scheduleMoodReminder()
        }
    }

    private fun shouldShowPermissionDialog(): Boolean {
        val permissionAsked = sharedPreferences.getBoolean("notification_permission_asked", false)
        val permissionGranted = sharedPreferences.getBoolean("notification_permission_granted", false)
        val lastPromptTime = sharedPreferences.getLong("last_permission_prompt_time", 0L)

        // If we've never asked, show the dialog
        if (!permissionAsked) {
            return true
        }

        // If permission was denied and it's been 48 hours, ask again
        if (!permissionGranted) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastPrompt = currentTime - lastPromptTime
            val fortyEightHoursInMillis = 48 * 60 * 60 * 1000L // 48 hours in milliseconds

            return timeSinceLastPrompt >= fortyEightHoursInMillis
        }

        // Otherwise, don't show the dialog
        return false
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