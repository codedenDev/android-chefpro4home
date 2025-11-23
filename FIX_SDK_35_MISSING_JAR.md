# Fix Missing android.jar for SDK Platform 35

## ❌ Problem
The Android SDK Platform 35 installation is incomplete - `android.jar` is missing from:
```
~/Library/Android/sdk/platforms/android-35/android.jar
```

This causes the build error:
```
Failed to transform android.jar to match attributes
Cannot create mockable android.jar
```

## ✅ Solution: Reinstall Android SDK Platform 35

### Using Android Studio SDK Manager (Recommended)

1. **Open Android Studio**
2. **Tools → SDK Manager** 
   - (or Preferences/Settings → Appearance & Behavior → System Settings → Android SDK)
3. Go to the **"SDK Platforms"** tab
4. Find **"Android 15.0 (API 35)"** or **"Android SDK Platform 35"**
5. **Uncheck** it (if checked)
6. Click **"Apply"** to remove the incomplete installation
7. **Check** "Android 15.0 (API 35)" again
8. Click **"Apply"** to download and install fresh
9. **Wait for installation to complete** (may take a few minutes)

### Verify Installation

After reinstalling, verify the file exists:

```bash
ls -lh ~/Library/Android/sdk/platforms/android-35/android.jar
```

You should see a file around **5-10 MB** in size.

### Alternative: Check SDK Platforms Tab

If "Android 15.0 (API 35)" shows as installed but incomplete:
1. In SDK Platforms tab, expand the "Android 15.0 (API 35)" entry
2. Make sure **"Android SDK Platform 35"** is checked
3. Click Apply to ensure complete installation

## 🔄 After Fixing

Once `android.jar` is installed:

```bash
cd /Users/richardkelly/Documents/Repo/android-chefpro4home
./gradlew clean
./gradlew assembleRelease
```

## 📋 Current Status

- ✅ **AGP**: 8.5.2 (IDE compatible)
- ✅ **compileSdk**: 35 (configured)
- ✅ **targetSdk**: 35 (configured)
- ❌ **SDK Platform 35**: Incomplete installation (missing android.jar)

Once you reinstall SDK Platform 35, everything will work!

