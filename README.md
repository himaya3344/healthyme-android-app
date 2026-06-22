# HealthyMe – Daily Routine Manager

HealthyMe is a personal wellness tracking Android application designed to help users manage daily habits, track moods, and stay hydrated. The app encourages users to build healthier routines through habit tracking, mood journaling, hydration reminders, and a home screen widget.

## Tech Stack

* Android Studio
* Kotlin
* XML Layouts
* SharedPreferences
* Material Design Components

## Features

### Daily Habit Tracker

* Add, edit, and delete daily habits
* Mark habits as complete
* View progress on the home screen
* Track daily routine consistency

### Mood Journal

* Log moods using emojis and notes
* View mood history in chronological order
* Share mood entries

### Hydration Reminders

* Set customizable hydration reminder intervals
* Receive notification reminders
* Open the app directly from notifications
* Uses AlarmManager for reliable scheduling

### Home Screen Widget

* Displays today’s habit completion percentage
* Updates when habits are marked complete
* Opens the app when tapped

## Implementation Details

* Uses SharedPreferences to store habits and moods as JSON
* Uses Fragment-based navigation with BottomNavigationView
* Uses Material Design components for a clean mobile UI
* Includes Android notification and alarm scheduling features
* Includes a home screen widget for quick habit progress tracking

## Required Android Permissions

The app uses the following permissions:

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Project Purpose

This project was developed to practice Android mobile application development using Kotlin while focusing on a real-life wellness problem. The goal is to help users become more mindful of their daily habits, emotional well-being, and hydration routines.

## Author

Himaya Rathnayaka
