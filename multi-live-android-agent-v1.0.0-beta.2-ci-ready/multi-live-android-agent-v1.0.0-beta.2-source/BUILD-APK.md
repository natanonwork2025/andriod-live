# Build APK

This source package includes `.github/workflows/android-apk.yml`.

The workflow installs JDK 17, Android SDK 35 and Gradle 8.9, runs `clean assembleDebug`, verifies the APK, creates a SHA-256 checksum, and uploads both as GitHub Actions artifacts.

Expected artifact:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/debug/app-debug.apk.sha256`

The debug APK is suitable for pilot/internal installation. A production release APK should use a private signing key that is never committed to the repository.
