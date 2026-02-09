# CookBook - Implementation Plan

> **Product:** Online Recipe Management System
> **Platform:** Android (Kotlin)
> **Architecture:** MVVM + Material 3
> **Backend:** Firebase (Auth, Firestore, Storage)

---

## Table of Contents
1. [Firebase Setup](#1-firebase-setup)
2. [Android Project Setup](#2-android-project-setup)
3. [Project Structure](#3-project-structure)
4. [Implementation Phases](#4-implementation-phases)
5. [Security Configuration](#5-security-configuration)
6. [Testing Checklist](#6-testing-checklist)

---

## 1. Firebase Setup

### 1.1 Create Firebase Project

1. Navigate to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Enter project name: **"CookBook"**
4. Disable Google Analytics (optional for MVP)
5. Click **"Create project"**

### 1.2 Register Android App

1. In Firebase Console, click the **Android icon** (⚙️)
2. Register app:
   - **Package name:** `com.yourname.cookbook` (must match your app)
   - **App nickname:** CookBook
   - **Debug signing certificate:** (optional for now)
3. Download `google-services.json`
4. Place it in: `app/` directory
5. Follow setup instructions for Gradle files

### 1.3 Enable Firebase Authentication

1. Go to **Authentication** → **Get Started**
2. Click **"Sign-in method"** tab
3. Enable **"Email/Password"**
4. Save changes

**Features to enable:**
- ✅ Email/Password authentication
- ❌ Email link (passwordless) - not needed for MVP
- ❌ Google Sign-In - future feature

### 1.4 Setup Firestore Database

1. Go to **Firestore Database** → **Create database**
2. Choose **"Start in test mode"** (temporary)
3. Select **region** (choose closest to target users)
4. Click **"Enable"**

**Initial Collections:**
```
├── users/
│   └── {userId}
│       ├── uid: String
│       ├── name: String
│       ├── email: String
│       └── favorites: Array<String>
│
└── recipes/
    └── {recipeId}
        ├── recipeId: String
        ├── name: String
        ├── description: String
        ├── category: String
        ├── cookingTime: String
        ├── difficulty: String
        ├── ingredients: Array<String>
        ├── steps: Array<String>
        ├── imageUrl: String
        ├── createdBy: String
        └── createdAt: Timestamp
```

### 1.5 Setup Firebase Storage

1. Go to **Storage** → **Get Started**
2. Start in **test mode**
3. Choose same region as Firestore
4. Create folder structure:
   ```
   ├── recipe_images/
   │   └── {userId}/{recipeId}.jpg
   ```

---

## 2. Android Project Setup

### 2.1 Prerequisites

- Android Studio (Latest version)
- Kotlin support
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- JDK 17+

### 2.2 Project-level Configuration

**`build.gradle.kts` (Project):**
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

### 2.3 App-level Configuration

**`build.gradle.kts` (App):**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.yourname.cookbook"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourname.cookbook"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Image Loading - Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Testing (Optional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
```

### 2.4 Android Manifest Permissions

**`AndroidManifest.xml`:**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application
        android:name=".CookBookApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.CookBook">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Timer Foreground Service -->
        <service
            android:name=".data.service.TimerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback" />
    </application>
</manifest>
```

---

## 3. Project Structure

```
app/src/main/java/com/yourname/cookbook/
│
├── CookBookApplication.kt          # Application class
├── MainActivity.kt                 # Main entry point
│
├── data/                          # Data layer
│   ├── model/                     # Data models
│   │   ├── User.kt
│   │   ├── Recipe.kt
│   │   └── ShoppingItem.kt
│   │
│   ├── repository/                # Repository implementations
│   │   ├── AuthRepository.kt
│   │   ├── RecipeRepository.kt
│   │   ├── StorageRepository.kt
│   │   └── UserRepository.kt
│   │
│   └── service/                   # Background services
│       └── TimerService.kt
│
├── domain/                        # Business logic (optional for MVP)
│   └── usecase/
│       ├── GetRecipesUseCase.kt
│       └── AddRecipeUseCase.kt
│
├── presentation/                  # UI layer
│   ├── navigation/
│   │   └── NavGraph.kt
│   │
│   ├── theme/                     # Material 3 theming
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── components/                # Reusable UI components
│   │   ├── RecipeCard.kt
│   │   ├── LoadingScreen.kt
│   │   └── ErrorScreen.kt
│   │
│   ├── auth/                      # Authentication screens
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── ForgotPasswordScreen.kt
│   │   └── AuthViewModel.kt
│   │
│   ├── home/                      # Home feed
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   │
│   ├── recipe/                    # Recipe features
│   │   ├── RecipeDetailScreen.kt
│   │   ├── AddRecipeScreen.kt
│   │   ├── EditRecipeScreen.kt
│   │   └── RecipeViewModel.kt
│   │
│   ├── favorites/                 # Favorites screen
│   │   ├── FavoritesScreen.kt
│   │   └── FavoritesViewModel.kt
│   │
│   ├── search/                    # Search functionality
│   │   ├── SearchScreen.kt
│   │   └── SearchViewModel.kt
│   │
│   ├── timer/                     # Cooking timer
│   │   ├── TimerScreen.kt
│   │   └── TimerViewModel.kt
│   │
│   ├── shopping/                  # Shopping list
│   │   ├── ShoppingListScreen.kt
│   │   └── ShoppingViewModel.kt
│   │
│   └── profile/                   # User profile
│       ├── ProfileScreen.kt
│       └── ProfileViewModel.kt
│
└── util/                          # Utility classes
    ├── Constants.kt
    ├── Result.kt                  # Sealed class for API results
    └── Extensions.kt
```

---

## 4. Implementation Phases

### Phase 1: Foundation & Authentication (Week 1)

#### Task 1.1: Setup Firebase & Dependencies
- [ ] Create Firebase project
- [ ] Add `google-services.json`
- [ ] Configure Gradle dependencies
- [ ] Test Firebase connection

#### Task 1.2: Create Data Models
**`data/model/User.kt`:**
```kotlin
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val favorites: List<String> = emptyList()
)
```

**`data/model/Recipe.kt`:**
```kotlin
data class Recipe(
    val recipeId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val cookingTime: String = "",
    val difficulty: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUrl: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### Task 1.3: Authentication Repository
**`data/repository/AuthRepository.kt`:**
```kotlin
class AuthRepository {
    private val auth = Firebase.auth

    fun signUp(email: String, password: String, name: String): Flow<Result<User>>
    fun signIn(email: String, password: String): Flow<Result<User>>
    fun signOut()
    fun resetPassword(email: String): Flow<Result<Unit>>
    fun getCurrentUser(): User?
}
```

#### Task 1.4: Auth UI Screens
- [ ] Splash Screen (auto-navigate based on auth state)
- [ ] Login Screen (email/password fields + validation)
- [ ] Register Screen (name, email, password fields)
- [ ] Forgot Password Screen

#### Task 1.5: Navigation Setup
- [ ] Create NavGraph with authentication routing
- [ ] Implement navigation destinations
- [ ] Handle back stack management

**Deliverable:** Working authentication flow (login, register, logout)

---

### Phase 2: Recipe Core Features (Week 2)

#### Task 2.1: Firestore Repository
**`data/repository/RecipeRepository.kt`:**
```kotlin
class RecipeRepository {
    private val db = Firebase.firestore

    fun getAllRecipes(): Flow<Result<List<Recipe>>>
    fun getRecipeById(id: String): Flow<Result<Recipe>>
    fun getRecipesByCategory(category: String): Flow<Result<List<Recipe>>>
    fun searchRecipes(query: String): Flow<Result<List<Recipe>>>
    fun addRecipe(recipe: Recipe): Flow<Result<String>>
    fun updateRecipe(recipe: Recipe): Flow<Result<Unit>>
    fun deleteRecipe(recipeId: String): Flow<Result<Unit>>
}
```

#### Task 2.2: Storage Repository
**`data/repository/StorageRepository.kt`:**
```kotlin
class StorageRepository {
    private val storage = Firebase.storage

    fun uploadRecipeImage(uri: Uri, userId: String, recipeId: String): Flow<Result<String>>
    fun deleteRecipeImage(imageUrl: String): Flow<Result<Unit>>
}
```

#### Task 2.3: Home Screen
- [ ] Create recipe feed with LazyColumn
- [ ] Implement RecipeCard component
- [ ] Add pull-to-refresh
- [ ] Show loading/error states
- [ ] Add category filter chips

#### Task 2.4: Recipe Detail Screen
- [ ] Display all recipe information
- [ ] Add favorite button (heart icon)
- [ ] Show ingredients list
- [ ] Show step-by-step instructions
- [ ] Add "Start Timer" button
- [ ] Add "Add to Shopping List" button

#### Task 2.5: Add/Edit Recipe Screen
- [ ] Image picker (gallery selection)
- [ ] Text fields for all recipe data
- [ ] Category dropdown
- [ ] Difficulty selector (Easy/Medium/Hard)
- [ ] Dynamic ingredient/step lists (add/remove)
- [ ] Form validation
- [ ] Upload progress indicator

**Deliverable:** Full recipe CRUD operations working

---

### Phase 3: Advanced Features (Week 3)

#### Task 3.1: Favorites System
**`data/repository/UserRepository.kt`:**
```kotlin
class UserRepository {
    fun addToFavorites(userId: String, recipeId: String): Flow<Result<Unit>>
    fun removeFromFavorites(userId: String, recipeId: String): Flow<Result<Unit>>
    fun getFavorites(userId: String): Flow<Result<List<Recipe>>>
}
```

- [ ] Implement add/remove favorite logic
- [ ] Create Favorites screen
- [ ] Update UI indicators

#### Task 3.2: Search Functionality
- [ ] Search bar with TextInput
- [ ] Search by recipe name
- [ ] Filter by category
- [ ] Real-time search results
- [ ] Empty state UI

#### Task 3.3: Cooking Timer
**`data/service/TimerService.kt`:**
```kotlin
class TimerService : Service() {
    private var countDownTimer: CountDownTimer? = null

    fun startTimer(durationMillis: Long)
    fun pauseTimer()
    fun resumeTimer()
    fun resetTimer()
    fun stopTimer()
}
```

**Features:**
- [ ] Timer UI with circular progress indicator
- [ ] Start/Pause/Resume/Reset buttons
- [ ] Foreground service for background operation
- [ ] Notification with timer controls
- [ ] Alarm sound on completion
- [ ] Vibration on completion

#### Task 3.4: Shopping List
- [ ] Add ingredients from recipe to list
- [ ] Check/uncheck items
- [ ] Remove items
- [ ] Clear completed items
- [ ] Persist list in Firestore

#### Task 3.5: Profile Screen
- [ ] Display user info
- [ ] Show user's uploaded recipes
- [ ] Logout button
- [ ] Delete account option (optional)

**Deliverable:** All MVP features working

---

### Phase 4: UI/UX Polish & Security (Week 4)

#### Task 4.1: Material 3 Theming
- [ ] Define color scheme (light/dark themes)
- [ ] Typography system
- [ ] Component styling
- [ ] Consistent spacing/padding
- [ ] Smooth animations/transitions

#### Task 4.2: Error Handling
- [ ] Network error messages
- [ ] Form validation errors
- [ ] Firebase error handling
- [ ] Retry mechanisms
- [ ] Toast messages for feedback

#### Task 4.3: Loading States
- [ ] Shimmer loading for recipe cards
- [ ] Progress indicators for uploads
- [ ] Skeleton screens
- [ ] Pull-to-refresh indicators

#### Task 4.4: Bottom Navigation
- [ ] Home tab
- [ ] Search tab
- [ ] Add Recipe tab (center, highlighted)
- [ ] Favorites tab
- [ ] Profile tab

**Deliverable:** Polished, production-ready UI

---

### Phase 5: Security & Testing

#### Task 5.1: Firestore Security Rules
**Deploy these rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // User documents
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // Recipe documents
    match /recipes/{recipeId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
                    && request.resource.data.createdBy == request.auth.uid;
      allow update, delete: if request.auth != null
                            && resource.data.createdBy == request.auth.uid;
    }
  }
}
```

#### Task 5.2: Storage Security Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /recipe_images/{userId}/{imageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && request.auth.uid == userId
                   && request.resource.size < 5 * 1024 * 1024; // 5MB limit
    }
  }
}
```

#### Task 5.3: Testing Checklist
- [ ] Test auth flow (signup, login, logout, password reset)
- [ ] Test recipe creation with image upload
- [ ] Test recipe editing/deletion (own recipes only)
- [ ] Test favorites add/remove
- [ ] Test search functionality
- [ ] Test timer in foreground/background
- [ ] Test timer notifications
- [ ] Test shopping list persistence
- [ ] Test offline behavior
- [ ] Test with low network speeds
- [ ] Test on multiple device sizes

**Deliverable:** Secure, tested application

---

## 5. Security Configuration

### 5.1 Firebase Free Tier Limits

| Service | Free Tier Limit |
|---------|----------------|
| Firestore Reads | 50,000/day |
| Firestore Writes | 20,000/day |
| Firestore Deletes | 20,000/day |
| Firestore Storage | 1 GB |
| Storage Downloads | 1 GB/day |
| Storage Uploads | 360 MB/day |
| Storage Total | 5 GB |
| Authentication Users | Unlimited |

### 5.2 Best Practices

1. **Image Optimization:**
   - Compress images before upload (max 1MB)
   - Use WebP format
   - Generate thumbnails for list views

2. **Query Optimization:**
   - Use pagination (limit queries)
   - Cache frequently accessed data
   - Implement offline persistence

3. **Security:**
   - Never trust client-side validation
   - Always validate on backend (Firestore rules)
   - Sanitize user inputs
   - Rate limit expensive operations

---

## 6. Testing Checklist

### 6.1 Functional Testing

#### Authentication
- [ ] User can register with valid email/password
- [ ] User cannot register with invalid email
- [ ] User cannot register with weak password (< 6 chars)
- [ ] User can login with correct credentials
- [ ] User cannot login with wrong credentials
- [ ] User receives password reset email
- [ ] User is redirected to home after login
- [ ] User is redirected to login after logout

#### Recipe Management
- [ ] User can view all recipes
- [ ] User can view recipe details
- [ ] User can add new recipe with image
- [ ] User can edit own recipe
- [ ] User cannot edit others' recipes
- [ ] User can delete own recipe
- [ ] User cannot delete others' recipes
- [ ] Images upload successfully
- [ ] Images display correctly

#### Favorites
- [ ] User can add recipe to favorites
- [ ] User can remove recipe from favorites
- [ ] Favorites persist after app restart
- [ ] Favorites screen shows only favorited recipes

#### Search
- [ ] Search returns matching recipes
- [ ] Search is case-insensitive
- [ ] Category filter works correctly
- [ ] Empty search shows all recipes

#### Timer
- [ ] Timer counts down correctly
- [ ] Timer can be paused/resumed
- [ ] Timer can be reset
- [ ] Timer works in background (app minimized)
- [ ] Notification shows timer status
- [ ] Alarm plays when timer completes
- [ ] Timer can be controlled from notification

#### Shopping List
- [ ] Items can be added from recipe
- [ ] Items can be checked/unchecked
- [ ] Items can be removed
- [ ] List persists after app restart

### 6.2 UI/UX Testing
- [ ] All screens follow Material 3 design
- [ ] App is responsive on different screen sizes
- [ ] Loading states are shown appropriately
- [ ] Error messages are clear and helpful
- [ ] Navigation is intuitive
- [ ] Back button behavior is correct
- [ ] Animations are smooth (60fps)

### 6.3 Performance Testing
- [ ] App launches in < 3 seconds
- [ ] Recipes load in < 2 seconds
- [ ] Image uploads show progress
- [ ] No memory leaks
- [ ] App doesn't crash under normal use
- [ ] Battery drain is acceptable

---

## 7. Key Metrics to Track

| Metric | Target | Tracking Method |
|--------|--------|----------------|
| User Signups | 100+ | Firebase Auth count |
| Recipes Added | 200+ | Firestore document count |
| App Crash Rate | < 2% | Crashlytics (future) |
| Avg Session Time | 5+ mins | Analytics (future) |
| Timer Usage Rate | 40%+ | Custom event tracking |

---

## 8. Future Enhancements (Post-MVP)

### Version 2.0 Features
- [ ] Recipe ratings and reviews
- [ ] Comment system
- [ ] Video recipe support
- [ ] Nutrition information calculator
- [ ] Offline mode (local caching)
- [ ] Social sharing
- [ ] Recipe import from URL
- [ ] Meal planner calendar
- [ ] Multiple themes
- [ ] Recipe collections/folders
- [ ] Collaborative recipe editing
- [ ] Recipe versioning

### Technical Improvements
- [ ] Migrate to Jetpack Compose fully
- [ ] Add unit tests (JUnit)
- [ ] Add UI tests (Espresso)
- [ ] Implement CI/CD pipeline
- [ ] Add Firebase Crashlytics
- [ ] Add Firebase Analytics
- [ ] Implement proper caching strategy
- [ ] Add image CDN
- [ ] Optimize Firestore queries with indexes

---

## 9. Resources

### Documentation
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Material 3 Guidelines](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

### Design Assets
- [Material Icons](https://fonts.google.com/icons)
- [Compose Material3 Components](https://developer.android.com/jetpack/compose/designsystems/material3)

### Tools
- [Firebase Console](https://console.firebase.google.com/)
- [Android Studio](https://developer.android.com/studio)
- [Figma](https://www.figma.com/) - for UI mockups

---

## 10. Timeline Summary

| Phase | Duration | Key Deliverables |
|-------|----------|------------------|
| Phase 1 | Week 1 | Authentication flow complete |
| Phase 2 | Week 2 | Recipe CRUD operations |
| Phase 3 | Week 3 | Timer, search, favorites |
| Phase 4 | Week 4 | UI polish, security rules |
| Phase 5 | Week 5 | Testing & deployment |

**Total Estimated Duration:** 5 weeks (for solo developer)

---

## Getting Started

1. **Setup Firebase** (Day 1)
   - Create project
   - Enable services
   - Download config file

2. **Configure Android Project** (Day 1)
   - Add dependencies
   - Setup project structure
   - Test Firebase connection

3. **Start Coding** (Day 2+)
   - Follow phase-by-phase implementation
   - Test each feature as you build
   - Commit code regularly

4. **Launch MVP** (Week 5)
   - Complete testing
   - Deploy security rules
   - Release to Play Store (internal testing)

---

**Good luck building CookBook!** 🍳
