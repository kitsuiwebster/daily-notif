# Daily Reminder App

A lightweight Android app built with Kotlin and Gradle (no Android Studio required). It sends a **daily notification** at a random time with motivational or practical messages. You can also define **special messages for specific dates** — the app runs infinitely, re-scheduling itself every day.

---

## ✨ Features

* 🕓 Random notification time from a customizable list (`hours.json`)
* 💬 Random daily messages (`notifications.json`)
* 📅 Custom date-based messages (`overrides.json`)
* 🔁 Automatically reschedules every day
* ⚙️ Works offline, no network required
* 🌓 Supports light/dark mode (Material 3 theme)

---

## 🛠 Tech Stack

* **Language:** Kotlin
* **Build system:** Gradle 8+
* **SDK:** Android 34 (minSdk 23)
* **Libraries:** AppCompat, Material3, ConstraintLayout, Activity KTX

---

## 🚀 Quick Start

```bash
# Clone & build
./gradlew assembleDebug

# Release build (signed)
export KEYSTORE_PWD="your_password"
export KEY_PWD="your_password"
./gradlew clean assembleRelease

# Install via ADB
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 📂 Configuration

All customizable data is stored under `res/raw/`:

* `notifications.json` → list of random messages
* `overrides.json` → special dates and their messages
* `hours.json` → hours allowed for notifications


