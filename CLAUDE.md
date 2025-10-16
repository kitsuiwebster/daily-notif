# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is an Android project built with Gradle and Kotlin (no Android Studio required).

### Essential Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore setup)
export KEYSTORE_PWD="your_password"
export KEY_PWD="your_password"
./gradlew clean assembleRelease

# Install via ADB
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Testing and Verification
- No automated test suite is present
- Manual testing is done through the app's UI buttons:
  - "Tester maintenant" for immediate notification
  - "Planifier dans 30 secondes" for short-term alarm testing

## Architecture Overview

### Core Components
The app follows a simple Android notification scheduling pattern:

1. **MainActivity.kt** - Simple UI with permission handling and test buttons
2. **AlarmScheduler.kt** - Calculates random notification times and schedules Android alarms
3. **ReminderReceiver.kt** - BroadcastReceiver that shows notifications and reschedules next alarm
4. **BootReceiver.kt** - Reschedules alarms after device reboot

### Data Layer (Repository Pattern)
- **MessageRepository.kt** - Reads random messages from `res/raw/notifications.json`
- **DateOverrideRepository.kt** - Handles special date messages from `res/raw/overrides.json`
- **HoursRepository.kt** - Manages allowed notification hours from `res/raw/hours.json`
- **NotificationHelper.kt** - Android notification channel creation and display

### Configuration Files (res/raw/)
- `notifications.json` - Array of default notification messages
- `overrides.json` - Map of YYYY-MM-DD dates to special messages  
- `hours.json` - Configuration for random hour selection with optional random minutes

### Key Android Features
- Requires `POST_NOTIFICATIONS` permission (Android 13+)
- Requires `SCHEDULE_EXACT_ALARM` permission (Android 12+)
- Uses `setExactAndAllowWhileIdle()` for reliable alarm delivery
- Automatically reschedules daily notifications in infinite loop
- Handles device reboots via BootReceiver

### Development Environment
- **Language**: Kotlin
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17 (required for AGP 8.5+)
- **Build Tools**: Gradle 8.7+, Android Gradle Plugin 8.5.2

### Dependencies
- AndroidX Core, AppCompat, Material3, ConstraintLayout
- Core library desugaring for java.time support on older Android versions

## Development Notes

### Keystore Configuration
The release build expects a `release.keystore` file in the project root with environment variables `KEYSTORE_PWD` and `KEY_PWD` for signing.

### Permission Flow
The app requests notification permissions first, then exact alarm permissions, with fallback handling for different Android versions.

### Data Format Examples
```json
// notifications.json
["Message 1", "Message 2"]

// overrides.json  
{"2025-12-25": "Special holiday message"}

// hours.json
{"hours": [8, 11, 14, 17, 20], "random_minute": true}
```