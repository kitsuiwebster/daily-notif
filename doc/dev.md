# Hello Bubble - Dev Guide

## Setup
```bash
# Java 17 required
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

## Build & Install Commands

### Debug Build (Fast)
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (Production)
```bash
export KEYSTORE_PWD="your_password"
export KEY_PWD="your_password"
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Full Clean Install
```bash
adb uninstall com.example.dailyreminder && ./gradlew clean assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk
```

## Development Workflow

1. **Edit code** (WebView CSS in `app/src/main/assets/index.html`)
2. **Build & install**: Use commands above
3. **Test**: Use app buttons to test notifications

## ADB Troubleshooting
```bash
# If device unauthorized
adb kill-server
adb start-server
adb devices

# Check if app installed
adb shell pm list packages | grep dailyreminder
```

## Architecture
- **UI**: WebView + CSS (no XML layouts)
- **Logic**: MainActivity.kt with JavaScript interface
- **Icons**: Adaptive icons with your PNG
- **Data**: JSON files in `res/raw/`