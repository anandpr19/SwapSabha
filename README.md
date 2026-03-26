# SwapSabha

**SwapSabha** is a hyper-local, peer-to-peer bartering and community skill-sharing Android application custom-built for GenZ college and university students.

Designed with a high-fidelity aesthetic, inspired by modern lifestyle apps, SwapSabha abandons traditional structural lines for a vibrant, borderless, and tonal layered experience featuring OLED-optimized dark modes and vibrant glassmorphism.

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![F-Droid Ready](https://img.shields.io/badge/F--Droid-Ready-success.svg)](https://f-droid.org/)

## 🚀 Features

### Core Skill-Sharing
- **Discover Feed**: A lifestyle-driven discovery engine to browse skills available on your campus using visually distinct Bento cards.
- **Bi-Directional Swaps**: Propose, negotiate, and execute skill exchanges seamlessly.
- **Dynamic Leaderboard**: Gamified campus rankings based on verified swapper contributions and hours taught.

### Reputation System
- **Rating Flow**: Post-swap verification using organic bottom sheets to securely rate the interaction and award specialized Tags.
- **Reputation Tiering**: Automatically evolving badges (Beginner → Intermediate → Expert → Master) attached to user profiles.
- **Hours Tracking**: Verifiable tracking of time spent teaching/swapping skills.

### Design System (Google Stitch Integration)
- **Organic Layouts**: All core UI components (Dashboard, Swaps Manager, Profile, and Dialogs) are migrated to an editorial, intentionally asymmetrical design natively using Material 3 and custom XML drawables.
- **Dynamic DayNight Mode**: Full palette adaptation emphasizing OLED blacks in dark mode and premium soft gradients in light mode.

## 🛠️ Architecture & Tech Stack

SwapSabha is built natively for Android using robust architecture patterns.

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) enforcing a unidirectional data flow (UDF).
- **UI Framework**: XML + Material 3 Components (Bento styling extensions).
- **Dependency Injection**: Koin / Hilt (Refer to `core/di`).
- **Data Persistence**: Room Database (Local Caching) + DataStore (Preferences).
- **Network Interface**: Retrofit2 & OkHttp3.
- **Asynchronous Processing**: Kotlin Coroutines & Flow.

> **Note on Safety:** The entire presentation layer utilizes type-safe **ViewBindings**, ensuring high stability and zero NullPointerExceptions during view inflation—even after aggressive UI migrations.


## 🏗️ Getting Started (Development)

### Prerequisites
- Android Studio Iguana (or newer)
- JDK 17
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34+

### Build Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/anandpr19/SwapSabha.git
   ```
2. Open the project `SwapSabha_Proj` in Android Studio.
3. Sync Gradle files.
4. Run the debug variant on a connected emulator or physical device.

```bash
# Terminal command for a quick debug build verification
./gradlew assembleDebug

```

## 🤝 Contributing

We welcome community contributions, specially from university developer clubs! Please read the [CONTRIBUTING.md](CONTRIBUTING.md) file before issuing a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
