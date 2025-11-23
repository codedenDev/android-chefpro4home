# Upgraded to Android SDK 35

## ✅ Changes Made

Updated `app/build.gradle.kts`:
- `compileSdk`: 34 → **35**
- `targetSdk`: 34 → **35**

This meets Google Play's requirement for targeting the latest SDK version.

## 📥 Install Android SDK Platform 35

The SDK Platform 35 needs to be installed. You can see it's partially there but missing `android.jar`.

### Using Android Studio (Recommended)

1. **Open Android Studio**
2. **Tools → SDK Manager** (or Preferences → Android SDK)
3. Go to the **"SDK Platforms"** tab
4. Check **"Android 15.0 (API 35)"** (or "Android SDK Platform 35")
5. Click **"Apply"** to download and install
6. Wait for installation to complete

### Verify Installation

After installing, verify the file exists:
```bash
ls -lh ~/Library/Android/sdk/platforms/android-35/android.jar
```

You should see a file around 5-10 MB in size.

## 🚀 Build After Installation

Once SDK Platform 35 is installed:

```bash
cd /Users/richardkelly/Documents/Repo/android-chefpro4home
./gradlew clean
./gradlew assembleRelease
```

Or build via Android Studio:
1. **Build → Generate Signed Bundle / APK**
2. Use your existing keystore (`app/release.keystore`)

## 📋 Google Play Requirements

- ✅ **Target SDK 35**: Required by Google Play for new app submissions and updates
- ✅ **Min SDK 24**: Kept at Android 7.0 (Nougat) for broad device support
- ✅ **Compile SDK 35**: Matches target SDK for consistency

## 🔍 Current Configuration

- **Application ID**: `com.chefpro4home`
- **Version Code**: 1
- **Version Name**: 1.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15.0)
- **Compile SDK**: 35 (Android 15.0)

