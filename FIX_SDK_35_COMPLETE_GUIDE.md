# Complete Fix Guide: SDK Platform 35 Missing android.jar

## 🔍 Current Situation
- SDK Platform 35 directory exists (~95MB)
- Critical file `android.jar` is **MISSING**
- This indicates a **corrupted/incomplete installation**

## ✅ Solution: Complete Reinstall

### Step 1: Remove Incomplete Installation

**Option A: Via Android Studio (Recommended)**
1. Open Android Studio
2. **Tools → SDK Manager**
3. Go to **"SDK Platforms"** tab
4. Find **"Android 15.0 (API 35)"**
5. **Uncheck** it
6. Click **"Apply"** 
7. Confirm removal when prompted
8. Wait for removal to complete

**Option B: Manual Removal (if Option A doesn't work)**
```bash
# Remove the incomplete platform
rm -rf ~/Library/Android/sdk/platforms/android-35

# Verify it's gone
ls ~/Library/Android/sdk/platforms/android-35 2>&1
```

### Step 2: Fresh Installation

1. In Android Studio SDK Manager (SDK Platforms tab)
2. **Check** "Android 15.0 (API 35)"
   - Make sure the checkbox is checked
   - Expand the entry to see sub-components
   - Ensure "Android SDK Platform 35" is checked
3. Click **"Apply"**
4. Wait for complete download/installation (this can take several minutes)
5. Watch the progress bar at the bottom

### Step 3: Verify Installation

After installation completes, verify:

```bash
ls -lh ~/Library/Android/sdk/platforms/android-35/android.jar
```

You should see:
- File exists
- Size: **5-10 MB** (typical size)
- Path: `android-35/android.jar`

### Step 4: Clean and Build

Once `android.jar` is confirmed:

```bash
cd /Users/richardkelly/Documents/Repo/android-chefpro4home
./gradlew clean
./gradlew assembleRelease
```

## 🔍 Troubleshooting

### If reinstall doesn't download android.jar:
1. Check your internet connection
2. In SDK Manager, expand "Android 15.0 (API 35)" entry
3. Make sure "Android SDK Platform 35" (not just tools) is checked
4. Try unchecking all, Apply, then checking again

### If Android Studio shows it as installed but file is missing:
1. File → Invalidate Caches / Restart
2. Restart Android Studio
3. Check SDK Manager again

### Alternative: Check SDK Build Tools
Make sure you also have:
- Android SDK Build-Tools 35.0.0 (or latest available)
- Android SDK Platform-Tools (latest)

## 📋 What Should Be Installed

For SDK 35 to work completely, you need:
- ✅ Android SDK Platform 35 (`android.jar` - ~5-10MB)
- ✅ Android SDK Build-Tools (any version 34.0.0+)
- ✅ Android SDK Platform-Tools

## 🎯 Expected Result

After successful reinstall:
```bash
$ ls -lh ~/Library/Android/sdk/platforms/android-35/android.jar
-rw-r--r--  1 user  staff   5.2M  ... android.jar
```

Then your build will work! ✅

