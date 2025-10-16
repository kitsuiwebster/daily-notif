# Hello Bubble

> ⚠️ **WARNING: If you are BubbleXGum, do not open /res/raw files**

A minimalist Android app built with **WebView + CSS** for a modern UI. Sends daily notifications at random times with motivational messages. Features an elegant blue & white design with smooth animations.

---

## ✨ Features

* 🫧 **Beautiful WebView UI** with CSS animations and glass effects
* 🕓 **Random notification times** from customizable hours (`hours.json`)
* 💬 **Random daily messages** (`notifications.json`)
* 📅 **Custom date-based messages** (`overrides.json`)
* 🔄 **Toggle enable/disable** with visual feedback
* 🧪 **Test notifications** with dedicated test messages
* 🔁 **Auto-reschedules** every day infinitely
* ⚡ **Offline first** - no network required

---

## 🎨 UI Design

* **Modern CSS interface** with blue & white theme
* **Adaptive icon** with perfect Android integration
* **Glass morphism effects** and smooth hover animations
* **No XML layouts** - pure WebView + CSS approach
* **Responsive design** optimized for mobile

---

## 🛠 Tech Stack

* **Language:** Kotlin
* **UI:** WebView + HTML/CSS/JavaScript
* **Build:** Gradle 8+ (no Android Studio required)
* **SDK:** Android 34 (minSdk 23)
* **Theme:** AppCompat Light
* **Icons:** Adaptive icons with PNG assets

---

## 🚀 Quick Start

```bash
# Setup Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Debug build & install
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release build (requires keystore)
export KEYSTORE_PWD="your_password"
export KEY_PWD="your_password"
./gradlew clean assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Development Workflow
```bash
# Full clean install (recommended)
adb uninstall com.example.dailyreminder && ./gradlew clean assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Project Structure

```
app/src/main/
├── assets/
│   └── index.html          # Main UI (WebView content)
├── java/.../
│   ├── MainActivity.kt     # WebView + JavaScript interface
│   ├── AlarmScheduler.kt   # Notification scheduling
│   ├── ReminderReceiver.kt # Notification handler
│   └── ...
├── res/
│   ├── mipmap-*/           # App icons (adaptive + PNG)
│   └── raw/                # Configuration files
│       ├── notifications.json
│       ├── overrides.json
│       └── hours.json
└── AndroidManifest.xml
```

---

## ⚙️ Configuration

All app behavior is controlled by JSON files in `res/raw/`:

### `notifications.json`
```json
["Motivational message 1", "Reminder text 2", "..."]
```

### `overrides.json`
```json
{"2025-12-25": "Special holiday message"}
```

### `hours.json`
```json
{"hours": [8, 11, 14, 17, 20], "random_minute": true}
```

---

## 🧪 Testing

The app includes built-in test functionality:

* **Test Now** - Immediate notification with test message
* **Test in 30s** - Scheduled test notification
* **Production mode** - Uses real messages from JSON files

---

## 📱 Permissions

* `POST_NOTIFICATIONS` - Android 13+ notification permission
* `SCHEDULE_EXACT_ALARM` - Android 12+ exact alarm scheduling
* `RECEIVE_BOOT_COMPLETED` - Auto-restart after device reboot

---

## 🔧 Development

See [doc/dev.md](doc/dev.md) for detailed development commands and workflow.

---

## 📄 License

This project is open source. Feel free to use and modify as needed.