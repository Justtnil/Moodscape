# Moodscape 🌄

**A modern, offline-first mood journaling application for Android, built with 100% Kotlin and Jetpack Compose.**

## About The Project

Moodscape is designed as a private, personal sanctuary for users to track their emotional landscape, discover patterns, and reflect on their well-being without relying on the cloud. All data is stored locally on-device, ensuring the app is 100% functional without an internet connection and that your personal information remains yours alone.

This project serves as a comprehensive case study in modern Android development, focusing on robust architecture, user-centric features, and a steadfast commitment to privacy.

---

## 📦 Releases

You can find the latest stable and pre-release APKs on the **[Releases](https://github.com/Justtnil/Moodscape/releases)** page of this repository.

---

## ✨ Features

### Core Journaling
* **Daily Mood Logging:** Log your mood with a custom emoji, a detailed note, and a score (1-5).
* **Visual Calendar:** A clean, full-screen calendar provides an at-a-glance heatmap of your mood history.
* **Customization:** Add, edit, and delete your own moods with unique emojis and names.
* **Categories (Backend):** The database supports tagging entries with categories like "Work" or "Health" for future analysis.

### Advanced Functionality
* **Offline Voice-to-Text:** Use the on-device `SpeechRecognizer` to dictate notes directly into your mood entries, completely offline.
* **Intelligent Reminders:** A `WorkManager`-powered background service schedules a daily notification to remind you to log your mood, but *only* if you haven't already.
* **Mood Logbook:** A dedicated screen to view, scroll, and filter your entire mood history by various time ranges (7, 14, 30, 90 days).

### Data & Insights
* **Secure Data Export:** Export your mood log for a selected time range as a PDF file directly to your device's storage using the Storage Access Framework.
* **Rule-Based Insights Engine:** An offline analysis engine that calculates and displays meaningful insights from your data, including:
    * Weekly mood trends.
    * Correlations between moods and days of the week.
    * Keyword clustering in notes for less positive moods.

---

## 🛠️ Tech Stack & Architecture

This project is built with a modern, robust, and scalable tech stack.

* **Language:** 100% [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a fully declarative and modern UI.
* **Architecture:** MVVM (Model-View-ViewModel)
* **Database:** [Room](https://developer.android.com/training/data-storage/room) for local persistence, including an FTS5 table for efficient full-text searching.
* **Asynchronous Programming:** Kotlin Coroutines and Flows for managing background threads and data streams.
* **Background Processing:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable, scheduled background tasks.
* **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) for handling screen transitions.

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

* Android Studio (latest stable version recommended)
* An Android device or emulator running API level 31 or higher

### Installation

1.  Clone the repo
    ```sh
    git clone [https://github.com/your-username/moodscape.git](https://github.com/your-username/moodscape.git)
    ```
2.  Open the project in Android Studio.
3.  Let Gradle sync and download the required dependencies.
4.  Build and run the app on your device or emulator.

---

## 🔮 Future Goals

The "Analysis" feature is designed to be a launchpad for more advanced functionality. The next major step is to integrate a small, on-device **Large Language Model (LLM)** to provide more nuanced, generative insights based on the user's mood and note history, all while maintaining complete user privacy.

---

## 🤝 Contributing

This project is a personal portfolio piece, but contributions, forks, and suggestions are welcome! Please feel free to open an issue or submit a pull request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## 📄 License

Distributed under the **GNU AGPLv3 License**. See `LICENSE` for more information.
