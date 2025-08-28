Moodscape 🌄

Moodscape is a modern, offline-first mood journaling application for Android, built entirely with Kotlin and Jetpack Compose. It's designed to be a private, personal sanctuary for users to track their emotional landscape, discover patterns, and reflect on their well-being without relying on the cloud.

This project was developed as a comprehensive case study in modern Android development, focusing on robust architecture, user-centric features, and a commitment to privacy.

✨ Core Features

    Daily Mood Logging: An intuitive interface to log your mood with a custom emoji, a detailed note, and a score (1-5).

    Visual Calendar: A clean, full-screen calendar that provides an at-a-glance heatmap of your mood history.

    Offline-First Architecture: All data is stored locally on your device using a robust Room database, ensuring the app is 100% functional without an internet connection.

    Customization:

        Custom Moods: Add, edit, and delete your own moods with unique emojis and names.

        Categories: (Backend implemented) Tag entries with categories like "Work," "Health," or "Relationships" for deeper analysis.

    Offline Voice-to-Text: Use the on-device SpeechRecognizer to dictate notes directly into your mood entries, completely offline.

    Intelligent Reminders: A WorkManager-powered background service schedules a daily notification to remind you to log your mood, but only if you haven't already.

    Mood Logbook: A dedicated screen to view, scroll, and filter your entire mood history.

    Data Export: Export your mood log for a selected time range as a PDF file directly to your device's storage using the Storage Access Framework.

    Rule-Based Insights Engine: An offline analysis engine that calculates and displays meaningful insights from your data, such as:

        Weekly mood trends.

        Correlations between moods and days of the week.

        Keyword clustering in your notes for less positive moods.

🛠️ Tech Stack & Architecture

    Language: 100% Kotlin

    UI: Jetpack Compose for a fully declarative and modern UI.

    Architecture: MVVM (Model-View-ViewModel)

    Database: Room for robust, local persistence.

        Includes an FTS5 table for efficient full-text searching in notes.

    Asynchronous Programming: Kotlin Coroutines and Flows for managing background threads and data streams.

    Background Processing: WorkManager for reliable, scheduled background tasks.

    Navigation: Navigation Compose for handling screen transitions.

    Dependency Injection: Manual (via ViewModelFactory) for simplicity.

🚀 Future Work

This project has a solid foundation, but there's always room to grow. The "Analysis" feature is designed to be a launchpad for more advanced functionality. The next major step would be to integrate a small, on-device Large Language Model (LLM) to provide more nuanced, generative insights based on the user's mood and note history.

Contributing

This project is a personal portfolio piece, but feel free to fork the repository, explore the code, and experiment with your own features!
