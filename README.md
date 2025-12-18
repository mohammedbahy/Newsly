# 📰 Newsly

<p align="center">
  <img src="app/src/main/res/drawable/newsly_logo.png" alt="Newsly Logo" width="120"/>
</p>

<p align="center">
  <b>Your Personal News Companion with AI-Powered Assistant</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase"/>
</p>

---

## 📱 About

**Newsly** is a modern Android news application that delivers personalized news from around the world. With an AI-powered chatbot assistant, users can interact with news content in a conversational way. The app features a beautiful dark theme UI, real-time bookmark synchronization, and push notifications for breaking news.

---

## ✨ Features

### 🗞️ News Features
- **Personalized News Feed** - Browse top headlines from your selected country
- **Category Filtering** - Explore news by categories (Technology, Sports, Business, Entertainment, Health, Science)
- **Infinite Scrolling** - Seamlessly load more articles as you scroll
- **Article Details** - Read full articles in an integrated WebView
- **Smart Search** - Find news articles with keyword search

### 🤖 AI Chatbot
- **News Assistant** - Ask questions about current events and news topics
- **Powered by Gemini AI** - Intelligent responses using Google's Gemini API
- **Chat History** - Conversations are saved and synced across devices
- **Floating Draggable Button** - Access chatbot from anywhere in the app

### 🔖 Bookmarks
- **Save Articles** - Bookmark articles for later reading
- **Cloud Sync** - Bookmarks sync across all your devices via Firestore
- **Real-time Updates** - Changes reflect instantly on all devices

### 🔔 Notifications
- **Breaking News Alerts** - Get notified when new articles are published
- **Customizable** - Enable/disable notifications from profile settings
- **Background Sync** - WorkManager handles periodic news checks

### 👤 User Management
- **Email/Password Authentication** - Traditional sign up and sign in
- **Google Sign-In** - One-tap authentication with Google
- **Profile Management** - View profile picture and manage settings
- **Country Selection** - Choose your preferred news region

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Navigation** | Jetpack Navigation Compose |
| **Dependency Injection** | Manual DI with AppModule |
| **Networking** | Retrofit + OkHttp |
| **Image Loading** | Coil |
| **Authentication** | Firebase Auth |
| **Database** | Cloud Firestore |
| **Local Storage** | DataStore Preferences |
| **Background Work** | WorkManager |
| **AI/ML** | Google Gemini API |
| **News API** | GNews API |

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── model/          # Data classes (User, NewsArticle, ChatMessage, etc.)
│   ├── remote/         # API services and Retrofit clients
│   └── repository/     # Repository classes for data operations
├── di/                 # Dependency injection module
├── navigation/         # Navigation graph and routes
├── notifications/      # WorkManager and notification utilities
└── ui/
    ├── screens/        # Composable screens (Home, Chat, Profile, etc.)
    ├── theme/          # App theme, colors, typography
    └── viewmodel/      # ViewModels for each screen
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK 29+ (Android 10+)
- Firebase account
- GNews API key
- Google Gemini API key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Newsly.git
   cd Newsly
   ```

2. **Set up Firebase**
   - Create a new project in [Firebase Console](https://console.firebase.google.com/)
   - Enable **Authentication** (Email/Password and Google Sign-In)
   - Enable **Cloud Firestore**
   - Download `google-services.json` and place it in `app/` directory
   - Add your SHA-1 and SHA-256 fingerprints for Google Sign-In

3. **Configure Firestore Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /users/{userId}/bookmarks/{bookmarkId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /users/{userId}/chat_messages/{chatId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
     }
   }
   ```

4. **Configure API Keys**
   
   Open `app/src/main/res/values/strings.xml` and add your API keys:
   ```xml
   <string name="gnews_api_key">YOUR_GNEWS_API_KEY</string>
   <string name="gemini_api_key">YOUR_GEMINI_API_KEY</string>
   <string name="default_web_client_id">YOUR_FIREBASE_WEB_CLIENT_ID</string>
   ```

5. **Get API Keys**
   - **GNews API**: Register at [GNews.io](https://gnews.io/) (Free tier: 100 requests/day)
   - **Gemini API**: Get key from [Google AI Studio](https://aistudio.google.com/app/apikey)
   - **Web Client ID**: Found in Firebase Console → Authentication → Sign-in method → Google

6. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click **Run**.

---

## 📸 Screenshots

<p align="center">
  <img src="screenshots/splash.png" width="200" alt="Splash Screen"/>
  <img src="screenshots/signin.png" width="200" alt="Sign In"/>
  <img src="screenshots/home.png" width="200" alt="Home Screen"/>
  <img src="screenshots/categories.png" width="200" alt="Categories"/>
</p>

<p align="center">
  <img src="screenshots/article.png" width="200" alt="Article Detail"/>
  <img src="screenshots/chat.png" width="200" alt="AI Chatbot"/>
  <img src="screenshots/bookmarks.png" width="200" alt="Bookmarks"/>
  <img src="screenshots/profile.png" width="200" alt="Profile"/>
</p>

> 📌 Add your screenshots to a `screenshots/` folder in the repository

---

## 🔐 Security

- All API communications use HTTPS
- Firebase Authentication handles user sessions securely
- Firestore Security Rules ensure data isolation between users
- API keys should be stored securely (consider using BuildConfig for production)

---

## 📋 Requirements

| Requirement | Version |
|-------------|---------|
| Min SDK | 29 (Android 10) |
| Target SDK | 36 (Android 15) |
| Kotlin | 1.9+ |
| Compose BOM | Latest |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Bahy**

- GitHub: https://github.com/mohammedbahy/

---

## 🙏 Acknowledgments

- [GNews API](https://gnews.io/) for providing news data
- [Google Gemini](https://deepmind.google/technologies/gemini/) for AI capabilities
- [Firebase](https://firebase.google.com/) for backend services
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI toolkit

---

<p align="center">
  Made with ❤️ using Kotlin & Jetpack Compose
</p>

