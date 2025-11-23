# Fix Android SDK Build Tools 34.0.0

## ✅ **Step 1: Already Done**
The corrupted build tools directory has been removed:
```
~/Library/Android/sdk/build-tools/34.0.0 (removed)
```

## 🔧 **Step 2: Reinstall Build Tools via Android Studio**

### Method 1: Using Android Studio SDK Manager (Recommended)

1. **Open Android Studio**
2. **Tools → SDK Manager** (or **File → Settings → Appearance & Behavior → System Settings → Android SDK**)
3. Go to the **"SDK Tools"** tab
4. Scroll down and find **"Android SDK Build-Tools"**
5. **Uncheck** "Android SDK Build-Tools 34.0.0" (if checked)
6. Click **"Apply"** to uninstall (if it was checked)
7. **Check** "Android SDK Build-Tools 34.0.0" again
8. Click **"Apply"** to download and install
9. Wait for installation to complete

### Method 2: Using SDK Manager from Terminal

If you have Android Studio's command line tools properly configured:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"

# Reinstall build tools
yes | sdkmanager "build-tools;34.0.0"
```

## ✅ **Step 3: Verify Installation**

After reinstalling, verify the build tools are complete:

```bash
ls -la ~/Library/Android/sdk/build-tools/34.0.0/core-lambda-stubs.jar
```

You should see the file exists.

## 🚀 **Step 4: Build Your Release**

Once the SDK is fixed, you can build:

```bash
cd /Users/richardkelly/Documents/Repo/android-chefpro4home
./gradlew bundleRelease    # For AAB (recommended for Google Play)
# or
./gradlew assembleRelease  # For APK
```

## 📍 **Build Output Location**

After building successfully, your files will be at:

**AAB (Android App Bundle):**
```
app/build/outputs/bundle/release/app-release.aab
```

**APK:**
```
app/build/outputs/apk/release/app-release.apk
```

## 🎯 **Alternative: Build via Android Studio**

If command-line build tools continue to have issues, you can always build via Android Studio:

1. **Build → Generate Signed Bundle / APK**
2. Use keystore: `app/release.keystore`
3. Password: `android123`
4. Alias: `release`
5. Key password: `android123`

This method works regardless of command-line SDK issues.

