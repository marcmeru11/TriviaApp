# TriviaApp

A modern Android trivia quiz application built with Jetpack Compose and Material Design 3, featuring questions from the [Open Trivia Database API](https://opentdb.com/).

## Overview

TriviaApp is a native Android application that provides an interactive trivia quiz experience with customizable game settings, streak tracking, and a clean Material You interface. The app follows the MVVM architecture with reactive state management using Kotlin StateFlow and Jetpack Compose for the UI layer. 

## Features

- **Quick Play Mode:** Start a quiz instantly with default settings.
- **Custom Game Configuration:** Choose category, difficulty, and number of questions.
- **Streak Tracking:** Track current and best answer streaks with persistent storage.
- **Session Token Management:** Automatic token handling to prevent duplicate questions. 
- **Material Design 3:** Modern UI with dynamic theming and dark mode support. 
- **Edge-to-Edge Display:** Immersive full-screen experience.

## Architecture

The app implements the **MVVM (Model-View-ViewModel)** pattern with three distinct layers:

### Presentation Layer
- `MainActivity`: Entry point that initializes the Compose UI tree.
- `TriviaApp`: Root composable that handles screen navigation.
- Screen composables: `HomeScreen`, `CustomGameScreen`, `QuizScreen`.

### State Management Layer
- `TriviaViewModel`: Manages navigation state and game configuration.
- `QuizViewModel`: Handles quiz logic, API coordination, and reactive state using StateFlow.

### Data Layer
- `RetrofitInstance`: Singleton for API communication.
- `TokenManager`: Manages session token lifecycle with SharedPreferences.
- `StreakManager`: Persistent best streak storage.

## Core Technologies

### Key Technologies
- **Kotlin**: Primary programming language.
- **Jetpack Compose**: Declarative UI framework.
- **Material Design 3**: UI components and theming.

### Dependencies
- Retrofit 2.9.0 for HTTP communication.
- Kotlin Coroutines 1.7.3 for asynchronous programming.
- Lifecycle ViewModel integration with Compose.
- Compose BOM for dependency version management.

## Build Configuration

- **Min SDK**: 24 (Android 7.0).
- **Target SDK**: 36.
- **Compile SDK**: 36.
- **Java Version**: 11.

## Key Feature Implementation

### Reactive State Management
The app uses `StateFlow` for real-time UI updates. Composables observe state changes with `collectAsState()`, triggering automatic recomposition on value change.

### Error Handling
Handles API response codes, HTTP exceptions, and rate limiting. Displays error messages with the `ErrorView` composable.

### Configuration Change Resilience
ViewModels survive configuration changes (rotation, theme changes) by being scoped to the activity lifecycle. The `startGame()` method avoids unnecessary initialization if the game configuration is unchanged.

## UI Components

Includes reusable Material Design 3 components:

- **StreakIndicator:** Shows current and best streaks with emoji icons.
- **DifficultyChip:** Color-coded difficulty badges.
- **Answer Cards:** Interactive cards providing feedback for correct/incorrect answers.
- **LoadingView:** Progress indicator shown during API calls.

## Additional Notes

- Application namespace: `com.marcmeru.triviagame`.
- Data source API: [opentdb.com](https://opentdb.com).
- Accessibility features included, such as RTL-aware icons and semantic roles for screen readers.
