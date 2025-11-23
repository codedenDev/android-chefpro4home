# Fix Missing android.jar Error

## Problem
The Android SDK Platform 34 installation is incomplete - `android.jar` is missing from:
```
~/Library/Android/sdk/platforms/android-34/android.jar
```

This causes the error:
```
Failed to transform android.jar to match attributes
Cannot create mockable android.jar
```

## ✅ Solution: Reinstall Android SDK Platform 34

### Option 1: Using Android Studio (Recommended)

1. **Open Android Studio**
2. **Tools → SDK Manager** (or Preferences/Settings → Android SDK)
3. Go to the **"SDK Platforms"** tab
4. **Uncheck** "Android 14.0 (API 34)" if checked
5. Click **"Apply"** to uninstall (if it was checked)
6. **Check** "Android 14.0 (API 34)" again
7. Click **"Apply"** to download and install fresh
8. Wait for installation to complete

### Option 2: Using Command Line (if SDK Manager works)

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"

# Accept licenses first
yes | sdkmanager --licenses

# Reinstall platform 34
sdkmanager "platforms;android-34"
```

## ✅ Verify Installation

After reinstalling, verify the file exists:

```bash
ls -lh ~/Library/Android/sdk/platforms/android-34/android.jar
```

You should see a file around 5-10 MB in size.

## 🔄 Clean and Rebuild

After fixing:

```bash
cd /Users/richardkelly/Documents/Repo/android-chefpro4home
./gradlew clean
./gradlew assembleRelease
```

## 📝 Gradle Version Compatibility

Your current versions are **compatible**:
- **Gradle**: 8.10.2 ✅
- **Android Gradle Plugin**: 8.5.2 ✅
- **Target SDK**: 34 ✅

The issue is purely the missing SDK platform file, not Gradle version incompatibility.

