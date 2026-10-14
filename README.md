# Kotha (কথা) — Voice Dictation Assistant

<p align="center">
  <img src="app/src/main/res/drawable/ic_kotha_mark.xml" width="96" height="96" alt="Kotha Logo" />
</p>

<p align="center">
  <b>A floating, continuous voice-dictation utility for Bangla (কথা) and other languages with instant accessibility text insertion across any Android app.</b>
</p>

<p align="center">
  <a href="../../releases/latest"><img src="https://img.shields.io/github/v/release/${{ github.repository }}?label=Download%20APK&color=brightgreen&logo=android" alt="Download APK" /></a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-blue?logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin%20%7C%20Jetpack%20Compose-purple?logo=kotlin" alt="Kotlin" />
</p>

---

## 📥 Download & Install

### Option 1: GitHub Releases (Recommended)
1. Go to the [**Latest Releases**](../../releases/latest) section of this repository.
2. Under **Assets**, click **`Kotha.apk`** to download directly to your Android device.
3. Tap the downloaded file to install. If prompted, tap **Settings** and enable **"Allow from this source"** (Install unknown apps).
4. Launch **Kotha** and follow the quick setup wizard.

### Option 2: Direct Repository File
- You can also download `Kotha.apk` directly from the root of this repository.

---

## ✨ Features

- 🎙️ **Floating Dictation Orb**: Draggable floating bubble that sits on your screen, accessible anytime from any app.
- 🇧🇩 **Bangla & Multilingual Voice Typing**: Continuous high-accuracy speech recognition fine-tuned for Bangla (bn-BD, bn-IN) and English.
- ⚡ **Direct Accessibility Text Insertion**: Injects recognized text directly into active input fields (WhatsApp, Messenger, Notes, Browsers) without replacing your existing keyboard.
- 💬 **Live Caption Preview**: Floating preview subtitle bubble shows dictation results as you speak.
- 🎛️ **Quick Settings Tile**: Toggle the floating orb directly from the Android notification quick settings shade.
- 🎨 **Custom Ink Color Themes**: Personalize orb glow, ink styling, and appearance.

---

## 🛠️ Permissions Required

- **Microphone**: Needed for speech recognition and dictation.
- **Accessibility Service (`Kotha Text Bridge`)**: Injects transcribed text into active text fields on your behalf.
- **Display over other apps (SYSTEM_ALERT_WINDOW)**: Displays the floating dictation orb and live caption preview.
- **Notifications**: Keeps background dictation service running reliably.

---

## 🚀 Building From Source

To build `Kotha.apk` locally with Gradle:

```bash
# Clone the repository
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>

# Make gradle wrapper executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 Automated Releases with GitHub Actions

This repository includes `.github/workflows/release.yml`. Whenever you want to publish a new release:
1. Go to **Actions** tab on GitHub.
2. Select **Build & Release Kotha APK**.
3. Click **Run workflow** (or push a tag like `git tag v1.0.0 && git push origin v1.0.0`).
4. GitHub Actions will automatically compile the APK and attach `Kotha.apk` to the new GitHub Release!
