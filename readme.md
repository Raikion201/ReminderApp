# 📅 Reminder App with Notifications

## 🔍 Project Overview

A mobile application for managing reminders with notification capabilities, developed as a course project for Mobile Programming at HCMUTE.

## 📚 Course Information

-   **University:** HCMC UNIVERSITY OF TECHNOLOGY AND EDUCATION
-   **Course Name:** Mobile Programming - MOPR331279E
-   **Instructor:** Dr. Huynh Xuan Phung
-   **Semester:** II (2024 - 2025)
-   **Group:** 12

## 👥 Team Members

| No. | Full Name         | Student ID |
| --- | ----------------- | ---------- |
| 1   | Nguyen Gia Huy    | 22110034   |
| 2   | Nguyen Nhat Quang | 22110065   |
| 3   | Nguyen Hai Trieu  | 22110081   |

## ✨ Features

-   📝 Create, edit, and delete reminders
-   ⏰ Set notification dates and times
-   🏷️ Categorize reminders
-   🔔 Receive push notifications
-   ✅ Mark reminders as complete
-   🔍 Search and filter functionality
-   🔐 User authentication

## 🛠️ Technical Stack

-   **Framework:** Android Jetpack Compose
-   **Language:** Kotlin
-   **Architecture:** MVVM
-   **Navigation:** Jetpack Navigation Compose
-   **UI Components:** Material 3
-   **State Management:** ViewModel & StateFlow

## 📂 Project Structure

```
/ReminderApp
├── /app                  # Main application module
│   ├── /build           # Build outputs
│   ├── /src             # Source code
│   │   ├── /main        # Main source set
│   │   │   ├── /java    # Kotlin source files
│   │   │   │   └── /com/example/reminderapp
│   │   │   │       ├── /data     # Data models and repositories
│   │   │   │       ├── /ui       # UI components and screens
│   │   │   │       ├── /util     # Utility functions
│   │   │   │       └── MainActivity.kt
│   │   │   ├── /res     # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── /androidTest # Instrumentation tests
│   │   └── /test        # Unit tests
│   ├── build.gradle.kts # App module build script
│   └── proguard-rules.pro
├── /gradle              # Gradle configuration
├── build.gradle.kts     # Project build script
├── settings.gradle.kts  # Project settings
└── README.md            # Project documentation
```

## 🚀 Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device:
    ```bash
    ./gradlew installDebug
    ```

## 📦 Key Dependencies

-   `androidx.compose:compose-bom`: Compose Bill of Materials
-   `androidx.compose.material3:material3`: Material 3 UI components
-   `androidx.navigation:navigation-compose`: Navigation for Compose
-   `androidx.lifecycle:lifecycle-viewmodel-compose`: ViewModel integration with Compose
-   `androidx.core:core-ktx`: Kotlin extensions for core Android libraries

## 🔄 Application Flow

1. Main dashboard displays upcoming reminders
2. Users can create new reminders with title, description, date, time, and category
3. Notifications are triggered at the specified time
4. Users can mark reminders as complete or delete them

## 🚀 Future Enhancements

-   📅 Calendar view integration
-   🔁 Recurring reminders
-   📍 Location-based reminders
-   👥 Shared reminders between users
-   🌓 Dark/light theme options

## 📄 License

This project is created for educational purposes as part of the Mobile Programming - MOPR331279E course.

## 🙏 Acknowledgements

Special thanks to Dr. Huynh Xuan Phung for guidance throughout the development process.
