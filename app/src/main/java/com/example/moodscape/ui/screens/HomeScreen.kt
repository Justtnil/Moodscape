package com.example.moodscape.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodscape.data.*
import com.example.moodscape.utils.isSameDay
import com.example.moodscape.utils.startOfDay
import com.example.moodscape.viewmodel.MainViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToLogbook: () -> Unit
) {
    val context = LocalContext.current
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showCustomizeMoodDialog by remember { mutableStateOf(false) }

    val allEntries by viewModel.allEntries.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val moodsForMonth by remember(allEntries, currentMonth) {
        derivedStateOf {
            allEntries.filter {
                val moodCalendar = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                moodCalendar.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                        moodCalendar.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
            }
        }
    }

    var currentSelectedMood by remember { mutableStateOf<MoodOption?>(null) }
    var currentNoteInput by remember { mutableStateOf("") }
    var currentSelectedCategory by remember { mutableStateOf<MoodCategory?>(null) }

    LaunchedEffect(selectedDate, allEntries) {
        val entry = allEntries.find { isSameDay(Calendar.getInstance().apply { timeInMillis = it.timestamp }, selectedDate) }
        currentSelectedMood = defaultMoodOptions.find { it.emoji == entry?.emoji }
        currentNoteInput = entry?.note ?: ""
        currentSelectedCategory = categories.find { it.id == entry?.categoryId }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var isListening by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(speechRecognizer, onResult = { spokenText ->
                currentNoteInput = spokenText
                isListening = false
            })
            isListening = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moodscape", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(elevation = CardDefaults.cardElevation(4.dp)) {
                CalendarView(
                    calendar = currentMonth,
                    onMonthChange = { newMonth -> currentMonth = newMonth },
                    onDateSelected = { date -> selectedDate = date },
                    selectedDate = selectedDate,
                    moods = moodsForMonth
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(selectedDate.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            MoodSelectionPanel(
                moodOptions = defaultMoodOptions,
                selectedMood = currentSelectedMood,
                onMoodSelected = { newMood -> currentSelectedMood = newMood },
                onCustomizeClick = { showCustomizeMoodDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = currentNoteInput,
                onValueChange = { currentNoteInput = it },
                label = { Text(if (isListening) "Listening..." else "Add a note (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Note")
                    }
                }
            )

            // CategorySelector and its Spacer are commented out as requested.
            /*
            Spacer(modifier = Modifier.height(16.dp))

            CategorySelector(
                categories = categories,
                selectedCategory = currentSelectedCategory,
                onCategorySelected = { currentSelectedCategory = it }
            )
            */

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    currentSelectedMood?.let { moodOption ->
                        val entry = MoodEntry(
                            timestamp = startOfDay(selectedDate.time).time,
                            emoji = moodOption.emoji,
                            note = currentNoteInput,
                            categoryId = currentSelectedCategory?.id,
                            moodScore = moodOption.score
                        )
                        viewModel.saveMoodEntry(entry)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(24.dp))

            RecentEntriesCard(
                moods = allEntries.take(5),
                onShowMoreClicked = onNavigateToLogbook
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCustomizeMoodDialog) {
        CustomizeMoodsDialog(
            moods = defaultMoodOptions.toMutableStateList(),
            onDismiss = { showCustomizeMoodDialog = false },
            onAddMood = { emoji, name, score ->
                // This won't work as expected since we're using a copy
                // In a real app, you'd want to manage this in a proper state
            },
            onDeleteMood = { moodOption ->
                // This won't work as expected since we're using a copy
                // In a real app, you'd want to manage this in a proper state
            }
        )
    }
}

private fun startListening(speechRecognizer: SpeechRecognizer, onResult: (String) -> Unit) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
            onResult(spokenText)
        }
        override fun onError(error: Int) { onResult("") }
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    speechRecognizer.startListening(intent)
}

@Composable
fun RecentEntriesCard(
    moods: List<MoodEntryWithCategory>,
    onShowMoreClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (moods.isEmpty()) {
                Text("No entries yet. Add a mood to get started!")
            } else {
                moods.forEach { mood ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(mood.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(mood.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val moodName = defaultMoodOptions.find { it.emoji == mood.emoji }?.name ?: "Custom Mood"
                                Text(moodName, fontWeight = FontWeight.SemiBold)
                                if (!mood.note.isBlank()) {
                                    Text(
                                        text = mood.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onShowMoreClicked) {
                    Text("Show More")
                }
            }
        }
    }
}


@Composable
fun CalendarView(
    calendar: Calendar,
    onMonthChange: (Calendar) -> Unit,
    onDateSelected: (Calendar) -> Unit,
    selectedDate: Calendar,
    moods: List<MoodEntryWithCategory>
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayOfWeekFormat = SimpleDateFormat("EE", Locale.getDefault())
    val haptic = LocalHapticFeedback.current

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

    val moodsByDay = moods.associateBy {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.timestamp
        cal.get(Calendar.DAY_OF_MONTH)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onMonthChange((calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) })
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }
            Text(text = monthFormat.format(calendar.time), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onMonthChange((calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) })
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            repeat(7) {
                Text(text = dayOfWeekFormat.format(cal.time).take(2), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                cal.add(Calendar.DAY_OF_WEEK, 1)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val emptyDays = (startDayOfWeek - Calendar.SUNDAY + 7) % 7

        for (i in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                for (j in 0 until 7) {
                    val dayIndex = i * 7 + j
                    val day = dayIndex - emptyDays + 1
                    if (day > 0 && day <= daysInMonth) {
                        val dateCalendar = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        val isSelected = isSameDay(dateCalendar, selectedDate)
                        val mood = moodsByDay[day]
                        DayCell(
                            day = day,
                            isSelected = isSelected,
                            moodEntry = mood,
                            onClick = { onDateSelected(dateCalendar) }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(day: Int, isSelected: Boolean, moodEntry: MoodEntryWithCategory?, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.toString(), fontSize = 14.sp)
            if (moodEntry != null) {
                Text(text = moodEntry.emoji, fontSize = 12.sp)
                moodEntry.categoryColorHex?.let { colorHex ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(android.graphics.Color.parseColor(colorHex)), CircleShape)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoodSelectionPanel(
    moodOptions: List<MoodOption>,
    selectedMood: MoodOption?,
    onMoodSelected: (MoodOption) -> Unit,
    onCustomizeClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (!isScrolling) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        flingBehavior = flingBehavior,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(moodOptions) { moodOption ->
            MoodPill(
                moodOption = moodOption,
                isSelected = moodOption == selectedMood,
                onClick = { onMoodSelected(moodOption) }
            )
        }
        item {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCustomizeClick()
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Customize Moods",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun MoodPill(moodOption: MoodOption, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = moodOption.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = moodOption.name, fontSize = 12.sp, color = contentColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<MoodCategory>,
    selectedCategory: MoodCategory?,
    onCategorySelected: (MoodCategory?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "No Category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("No Category") },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeMoodsDialog(
    moods: List<MoodOption>,
    onDismiss: () -> Unit,
    onAddMood: (String, String, Int) -> Unit,
    onDeleteMood: (MoodOption) -> Unit
) {
    var emojiText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Your Moods") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(moods) { mood ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mood.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(mood.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDeleteMood(mood)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Mood")
                            }
                        }
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Add New Mood", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = emojiText,
                    onValueChange = { emojiText = it },
                    label = { Text("Emoji") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Mood Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { scoreText = it },
                    label = { Text("Mood Score (1-5)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val score = scoreText.toIntOrNull()
                    if (emojiText.isNotBlank() && nameText.isNotBlank() && score != null && score in 1..5) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddMood(emojiText, nameText, score)
                        emojiText = ""
                        nameText = ""
                        scoreText = ""
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}