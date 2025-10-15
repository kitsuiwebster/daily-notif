# Guide complet – Daily Reminder Android (sans Android Studio)

Ce document décrit, **de A à Z**, comment construire, signer, installer et tester l’app **Daily Reminder** (1 notification locale/jour à heure aléatoire, messages aléatoires + overrides par date), **sans Android Studio**.

> Testé avec **Java 17**, **Gradle 8.7**, **AGP 8.5.x**, **SDK Android 34**.

---

## 1) Pré‑requis

- **Java JDK 17** (obligatoire pour AGP 8.5+)
- **Gradle 8.7+** (ou wrapper généré via `gradle wrapper`)
- **Android SDK** (platform-tools, build-tools, cmdline-tools, platforms;android-34)
- Un téléphone Android (débogage USB facultatif)

### 1.1 Vérifier / installer Java 17
```bash
java -version
# Si ≠ 17, avec SDKMAN:
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.12-tem
sdk use java 17.0.12-tem
```

### 1.2 Installer les command line tools Android (sans Android Studio)
```bash
export ANDROID_HOME="$HOME/Android/sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools" "$ANDROID_HOME/platform-tools"

# Linux
a=(commandlinetools-linux-11076708_latest.zip)
cd ~/Downloads
wget https://dl.google.com/android/repository/$a -O cmdline-tools.zip
unzip -q cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"

# PATH pour la session courante
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

# Installer composants SDK
sdkmanager --install "platform-tools" "cmdline-tools;latest" "build-tools;34.0.0" "platforms;android-34"
yes | sdkmanager --licenses
```

> Pour macOS : remplace l’archive par `commandlinetools-mac-11076708_latest.zip`.

---

## 2) Préparer le projet

Structure minimale du projet (extrait) :
```
app/
  src/main/
    AndroidManifest.xml
    java/com/example/dailyreminder/
      MainActivity.kt
      AlarmScheduler.kt
      ReminderReceiver.kt
      BootReceiver.kt
      NotificationHelper.kt
      MessageRepository.kt
      HoursRepository.kt
      DateOverrideRepository.kt
    res/
      values/strings.xml
      values/themes.xml   # ← thème AppCompat/Material requis
      raw/
        notifications.json
        overrides.json
        hours.json
build.gradle.kts
settings.gradle.kts
gradle.properties
```

### 2.1 `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "daily-reminder"
include(":app")
```

### 2.2 `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
# Laisser vide ou pointer vers JDK 17 exact
# org.gradle.java.home=
```

### 2.3 `build.gradle.kts` (racine)
```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
```

### 2.4 `app/build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.dailyreminder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dailyreminder"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Pour java.time sur API < 26
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }

    signingConfigs {
        create("release") {
            // Chemin vers le keystore (voir §4)
            storeFile = rootProject.file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PWD") ?: "<ton_mdp>"
            keyAlias = "release"
            keyPassword = System.getenv("KEY_PWD") ?: "<ton_mdp>"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug { }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.2")

    // Desugaring java.time
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
```

### 2.5 `AndroidManifest.xml`
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
  <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>

  <application
      android:label="Daily Reminder"
      android:allowBackup="true"
      android:theme="@style/Theme.DailyReminder">

    <activity
        android:name=".MainActivity"
        android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
      </intent-filter>
    </activity>

    <receiver android:name=".ReminderReceiver" android:exported="false"/>

    <receiver android:name=".BootReceiver" android:enabled="true" android:exported="false">
      <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
      </intent-filter>
    </receiver>
  </application>
</manifest>
```

### 2.6 Thème (obligatoire) – `res/values/themes.xml`
```xml
<resources>
    <style name="Theme.DailyReminder" parent="Theme.Material3.DayNight.NoActionBar" />
</resources>
```

### 2.7 Ressources `raw/`
- `notifications.json` : liste de phrases par défaut (tableau JSON)
- `overrides.json` : map `YYYY-MM-DD` → message
- `hours.json` : configuration des heures aléatoires

Exemples :
```json
// notifications.json
[
  "Coucou 👋",
  "Hydrate-toi 💧",
  "Tu gères ✨",
  "Respire 🌿",
  "Souris 🙂"
]
```
```json
// overrides.json
{
  "2025-12-25": "Joyeux Noël 🎄",
  "2026-01-01": "Bonne année ✨"
}
```
```json
// hours.json
{
  "hours": [8, 11, 14, 17, 20, 22],
  "random_minute": true
}
```

---

## 3) Code (rappels des rôles)

- **MainActivity.kt** : UI minimaliste (boutons *Activer les rappels*, *Tester maintenant*, *Planifier dans 30s*), demande permissions et exact alarms.
- **AlarmScheduler.kt** : calcule l’heure aléatoire à partir de `HoursRepository` et programme l’alarme (`setExactAndAllowWhileIdle`).
- **ReminderReceiver.kt** : affiche une notif via `NotificationHelper`, choisit un message override si présent (`DateOverrideRepository`), puis replanifie le lendemain (boucle infinie).
- **BootReceiver.kt** : reprogramme au reboot.
- **MessageRepository.kt** : lit `res/raw/notifications.json` et renvoie une phrase aléatoire.
- **HoursRepository.kt** : lit `res/raw/hours.json`, renvoie (heure, minute) aléatoires.
- **DateOverrideRepository.kt** : lit `res/raw/overrides.json`, renvoie le message du jour s’il existe.
- **NotificationHelper.kt** : crée le channel et affiche la notification.

> Points importants : canal de notif (O+), `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM` (Android 12+), et **desugaring** pour `java.time`.

---

## 4) Signature release

### 4.1 Créer le keystore (une fois)
```bash
# À la racine du projet
keytool -genkeypair -v -keystore release.keystore -alias release \
  -storetype PKCS12 -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass <ton_mdp> -keypass <ton_mdp> \
  -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
```

### 4.2 Variables d’env (optionnel mais pratique)
```bash
export KEYSTORE_PWD='<ton_mdp>'
export KEY_PWD='<ton_mdp>'
```

### 4.3 Build release signé automatiquement par Gradle
```bash
./gradlew clean assembleRelease
# APK final signé :
ls app/build/outputs/apk/release/app-release.apk
```

> Alternative (manuelle) : `zipalign` + `apksigner` via `$ANDROID_HOME/build-tools/<ver>/`.

---

## 5) Installation sur le téléphone

### 5.1 Sans ADB (le plus simple)
- Copie `app-release.apk` sur le téléphone (USB MTP, Drive, email…)
- Ouvre **Fichiers** → `app-release.apk`
- Autorise **Installer applis inconnues** pour l’app source si demandé
- Sur Android 13+ : au 1er lancement, accepte **POST_NOTIFICATIONS**
- Sur Android 12+ : autorise **Exact Alarms** (l’app t’amène sur l’écran système)

### 5.2 Via ADB (USB)
```bash
adb devices
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 6) Tests rapides

- **Ouvrir l’app** → *Activer les rappels* (accepte les permissions)
- **Tester maintenant** → affiche une notif immédiate
- **Planifier dans 30s** → vérifie `AlarmManager` et les modes d’économie d’énergie (Doze)
- **Vérifier les données** :
  - Modifier `notifications.json` et `overrides.json`, relancer l’app
  - `hours.json` : ajuster la liste d’heures, `random_minute` true/false

---

## 7) Dépannage – erreurs fréquentes & correctifs

### A) `Value '' given for org.gradle.java.home ... invalid`
- **Cause** : `org.gradle.java.home=` vide dans `gradle.properties`.
- **Fix** : supprime la ligne, ou mets le chemin JDK 17 exact. Vérifie `./gradlew -v` (JVM: 17).

### B) `SDK location not found`
- **Cause** : SDK non déclaré.
- **Fix** : `echo "sdk.dir=$HOME/Android/sdk" > local.properties` et/ou exporte `ANDROID_HOME` et `PATH`.

### C) `android:exported needs to be explicitly specified`
- **Cause** : activité avec `<intent-filter>` sans `android:exported`.
- **Fix** : `android:exported="true"` sur l’`activity` LAUNCHER.

### D) Installation bloquée sur “Installing…`
- **Causes** : ancienne app signée différemment, source d’installation non autorisée, Play Protect.
- **Fix** : désinstaller l’ancienne app, autoriser la source, désactiver Play Protect temporairement. En cas de doute, signer en **release**.

### E) `Keystore ... not found` ou `Given final block not properly padded`
- **Cause** : chemin keystore incorrect, alias/mots de passe incohérents.
- **Fix** : chemin correct (`rootProject.file("release.keystore")`), alias `release`, mdp `KEYSTORE_PWD` = `KEY_PWD` si identiques, `storeType = "pkcs12"`.

### F) Crash : `You need to use a Theme.AppCompat theme`
- **Cause** : aucun thème AppCompat/Material appliqué.
- **Fix** : ajouter `themes.xml` et `android:theme` sur `<application>`.

### G) Crash sur appareils < Android 8 (API 26) lié à `java.time`
- **Fix** : activer **desugaring** + dépendance `desugar_jdk_libs` (voir §2.4), puis rebuild.

### H) ADB ne voit pas l’appareil
- **Fix** : activer Débogage USB et accepter la clé RSA, sinon `adb kill-server && adb start-server`. En Wi‑Fi : mettre à jour `platform-tools`.

---

## 8) Personnalisation

- **Heures** : `hours.json` → liste d’heures (0–23), minute aléatoire on/off
- **Messages** : `notifications.json` → pool libre
- **Dates spéciales** : `overrides.json` → `YYYY-MM-DD` → message du jour
- **Boucle infinie** : l’alarme est reprogrammée chaque jour automatiquement

Idées d’extensibilité : compteur “Jour N”, plages horaires (matin/aprem/soir), UI pour éditer les listes depuis l’app, export/import JSON.

---

## 9) Commandes récap
```bash
# Wrapper & versions
gradle wrapper --gradle-version 8.7
./gradlew -v

# Build debug
./gradlew assembleDebug

# Build release signé (config Gradle)
export KEYSTORE_PWD='<mdp>'
export KEY_PWD='<mdp>'
./gradlew clean assembleRelease

# APK
ls app/build/outputs/apk/debug/app-debug.apk
ls app/build/outputs/apk/release/app-release.apk

# ADB (optionnel)
adb devices
adb install -r app/build/outputs/apk/release/app-release.apk

# Logs crash
adb logcat -s AndroidRuntime
```

---

**Fin.** Ce guide te permet de repartir de zéro et d’arriver à une app fonctionnelle identique à ton installation actuelle, étape par étape. Besoin d’un PDF prêt à imprimer ? Je peux l’exporter en un clic.