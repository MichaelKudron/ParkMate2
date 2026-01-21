# ParkMate2

ParkMate is an Android parking session manager application built with Kotlin, following MVVM architecture.

## Features

- **Start Parking Session**: Record parking location with GPS coordinates and optional notes
- **Active Session Tracking**: View real-time distance and direction to parked car
- **Parking Meter Timer**: Set paid parking duration with automatic reminders
- **Session History**: Browse and search past parking sessions
- **Share Sessions**: Export session details with address and coordinates
- **Settings**: Configure reminder preferences and distance units

## Architecture

- **MVVM Pattern**: ViewModels manage UI-related data
- **Navigation Component**: Single Activity with multiple Fragments
- **Room Database**: Local persistence with migrations
- **Retrofit**: Reverse geocoding via Nominatim API
- **WorkManager**: Background notifications for parking meter expiry
- **Location Services**: FusedLocationProvider for GPS tracking
- **Sensors**: Compass integration for direction arrow

## Technical Stack

- **Language**: Kotlin
- **UI**: XML layouts with Material Design Components
- **Database**: Room SQLite
- **Networking**: Retrofit + Gson
- **Dependency Injection**: Manual injection via Application class
- **Location**: Google Play Services Location API
- **Background Work**: WorkManager

## Project Structure

```
app/
├── src/main/
│   ├── java/com/parkmate/
│   │   ├── ParkMateApplication.kt
│   │   ├── MainActivity.kt
│   │   ├── data/              # Data layer (Room, Retrofit, Repository)
│   │   ├── ui/                # UI layer (Fragments, ViewModels)
│   │   ├── workers/           # WorkManager workers
│   │   └── utils/             # Helper classes
│   └── res/                   # Resources (layouts, drawables, etc.)
```

## Building

This project requires:
- Android Studio Arctic Fox or later
- Android SDK 24+
- Gradle 8.0+

To build:
```bash
./gradlew assembleDebug
```

## Permissions

The app requires:
- `ACCESS_FINE_LOCATION` - GPS location tracking
- `ACCESS_COARSE_LOCATION` - Approximate location
- `INTERNET` - Reverse geocoding
- `POST_NOTIFICATIONS` - Parking meter reminders

## Database Migration

The app includes a migration from version 1 to 2 that adds the `accuracyMeters` column to the `parking_sessions` table.