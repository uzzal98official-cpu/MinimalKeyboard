# Minimal Keyboard (Android)

A small, offline-first English Android keyboard prototype with a dark QWERTY layout, emoji panel, number/symbol mode, and horizontal spacebar cursor movement.

## Privacy

- The app requests no `INTERNET` permission.
- No analytics, telemetry, ads, or remote services are included.
- Text is entered through Android's `InputConnection`; the app does not save or transmit typed text.

## Build locally

Requirements: JDK 17, Android SDK Platform 35, Android Build Tools, and Gradle 8.9.

From this project directory:

```bash
gradle :app:assembleDebug
```

The debug APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build using GitHub Actions

The repository includes `.github/workflows/android-build.yml`. Push the project to a GitHub repository, then open **Actions → Android APK Build → Run workflow**. Download the `MinimalKeyboard-debug-apk` artifact from the completed run.

## Install and enable

1. Install `app-debug.apk` on your Android phone.
2. Open Minimal Keyboard.
3. Tap **Enable Minimal Keyboard** and enable it in Android settings.
4. Return to the app and tap **Select Minimal Keyboard**.
5. Choose Minimal Keyboard in the input-method picker.

## Current limitations

This is an initial prototype, not a fully tested production keyboard. Emoji coverage is a small built-in set. Cursor movement uses Android DPAD key events and may behave differently in some apps. Test it in ordinary text fields before relying on it. Password and other sensitive fields remain governed by Android's input-method behavior.
