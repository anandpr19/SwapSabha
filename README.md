# SwapSabha 🔄

**SwapSabha** is an open-source, community-driven Android application designed to facilitate peer-to-peer skill swapping. Instead of paying for classes, users can barter their expertise. Teach what you know, and learn what you want—for free!

---

## 🌟 Core Features

*   **Firebase Authentication & Profiles:** Secure email/password login, customizable user profiles, and avatars.
*   **Skill Discovery:** Browse an intuitive swipe-friendly interface to discover skills offered by people nearby or globally.
*   **Real-time Skill Swaps:** Propose, accept, and manage 1-on-1 skill swap sessions with full status tracking (PENDING -> ACCEPTED -> COMPLETED).
*   **Reputation & Badges System:** After an exchange, users rate each other. High ratings unlock unique badges and boost your standing on the global leaderboard.
*   **Dynamic Dashboard:** Keep track of your learning goals, pending swaps, recent encounters, and community stats all on the main dashboard.

---

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **UI Toolkit:** XML Layouts / ViewBinding + Material Design 3
*   **Backend & DB:** Firebase Authentication & Cloud Firestore (NoSQL)
*   **Concurrency:** Kotlin Coroutines & Flow
*   **Image Loading:** Glide (for user profile avatars)

---

## 📋 Prerequisites

To build and run this project, you need:
*   [Android Studio](https://developer.android.com/studio) (Koala or newer recommended)
*   JDK 17 or higher
*   Firebase Project 

---

## 🚀 Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/anandpr19/SwapSabha.git
    cd SwapSabha
    ```
2.  **Add Firebase Configuration:**
    *   Create a project on the [Firebase Console](https://console.firebase.google.com/).
    *   Enable **Authentication** (Email/Password) and **Firestore Database**.
    *   Download your `google-services.json` file and place it inside the `app/` directory.
3.  **Build the App:**
    *   Open the project in Android Studio.
    *   Sync Gradle files.
    *   Run the app on an Android Emulator or physical device (`Shift + F10`).

---

## 🗄️ Firebase Setup & Rules

You will need to deploy these custom security rules down to your Firestore Database so that Swaps, Profile reads, and Peer Ratings are fully operational:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users — read public, update allowed for peer reputation calculations
    match /users/{userId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null; 
      allow delete: if false;
    }
    
    // Skills — read public, modify only by owner
    match /skills/{skillId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Learning Goals — modify only by owner
    match /learningGoals/{goalId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }

    // Swaps — restricted exclusively to the student and teacher
    match /swaps/{swapId} {
      allow read, update: if request.auth != null && (resource.data.requesterId == request.auth.uid || resource.data.teacherId == request.auth.uid);
      allow create: if request.auth != null;
    }

    // Ratings — read public, create allowed
    match /ratings/{ratingId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if false;
    }
  }
}
```

*Note: You also need to create **Composite Indexes** via the Firebase Console for queries like Leaderboard arrays or reverse chronologically ordered active swaps. Look for the auto-generated links inside your Android Studio Logcat for easy one-click index setup.*

---

## 🤝 Contributing / Design Modifications
For those working to publish this on **F-Droid**:
- Strip any unnecessary Google Analytics or proprietary non-open-source dependencies (Firebase is compatible with F-Droid as long as open-source plugins like FOSS drop-ins are configured correctly if strict adherence is needed, though standard Firebase usually requires the GMS services wrapper). 
- Ensure your `colors.xml` and `themes.xml` provide solid Dark/Light mode contrasting before final release.

## 📜 License
This project is open-source. Be sure to configure the repository branch with the proper Open Source License (like GNU GPLv3 or MIT) before releasing via F-Droid.

---
*Built with ❤️ for the community. Share your skills, not your money!*
