# HealthyMe – Daily Routine Manager

A wellness app to help users manage daily habits, track moods, and stay hydrated.

## Required Manifest Entries

Add these permissions and declarations to your `AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Permissions -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.HealthyMe">

        <!-- MainActivity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Widget Provider -->
        <receiver
            android:name=".widget.HabitWidgetProvider"
            android:exported="false">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
                <action android:name="com.example.healthyme.action.UPDATE_WIDGET" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/habit_widget_info" />
        </receiver>

        <!-- Hydration Alarm Receiver -->
        <receiver
            android:name=".utils.HydrationAlarmReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.example.healthyme.action.HYDRATION_ALARM" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

## Features

1. **Daily Habit Tracker**
   - Add/edit/delete habits
   - Mark habits as complete
   - Progress tracking on home screen and widget

2. **Mood Journal**
   - Log moods with emoji and notes
   - Share mood entries
   - Chronological mood history

3. **Hydration Reminders**
   - Customizable reminder interval
   - Notification with direct app launch
   - Uses AlarmManager for reliable scheduling

4. **Home Screen Widget**
   - Shows today's habit completion percentage
   - Updates automatically when habits are marked complete
   - Tappable to open app

## Implementation Notes

- Uses SharedPreferences to store habits and moods as JSON
- Material Design components for clean UI
- MVVM-ish architecture with data models and adapters
- Fragment-based navigation with BottomNavigationView