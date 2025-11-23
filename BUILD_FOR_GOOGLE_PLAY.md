# Building for Google Play Console

This guide will help you build a signed APK or AAB (Android App Bundle) that you can upload to Google Play Console for closed testing.

## Two Ways to Build

### 🎯 **Recommended: Use Android Studio (Easiest)**

If you're using Android Studio, this is the simplest approach:

1. Open your project in Android Studio
2. Go to **Build** → **Generate Signed Bundle / APK**
3. Choose **Android App Bundle** (recommended) or **APK**
4. **First time?** Click "Create new..." to create a keystore, or **Use existing** if you already have one
5. Fill in your keystore information and passwords
6. Select **release** build variant
7. Click **Finish**
8. Your signed AAB/APK will be in the `app/release/` folder

**Done!** You can now upload this file to Google Play Console.

---

### ⚙️ **Alternative: Command Line**

If you prefer using the terminal or need to automate builds:

## Prerequisites

- Java JDK 17 or higher installed
- Android SDK installed and configured
- A keystore file for signing your app

## Step 1: Create a Keystore

If you don't already have a keystore file, create one using the following command:

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore app/release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

You'll be prompted to enter:
- A password for the keystore (save this securely!)
- Your name and organizational information
- A password for the key (can be the same as keystore password)

**IMPORTANT**: Save your keystore password and key password in a secure location. You'll need these to update your app in the future.

## Step 2: Configure Signing

1. Copy the example keystore properties file:
   ```bash
   cp keystore.properties.example keystore.properties
   ```

2. Edit `keystore.properties` and fill in your actual values:
   ```properties
   storeFile=../app/release.keystore
   storePassword=your_actual_store_password
   keyAlias=release
   keyPassword=your_actual_key_password
   ```

3. **DO NOT commit `keystore.properties` to version control** - it's already in `.gitignore`

## Step 3: Build Release APK or AAB

### Option A: Build APK (for testing or legacy upload)

```bash
./gradlew assembleRelease
```

The APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

### Option B: Build AAB (Android App Bundle - Recommended for Google Play)

```bash
./gradlew bundleRelease
```

The AAB will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

**Note**: Google Play Console prefers AAB format as it allows Google to generate optimized APKs for different device configurations.

## Step 4: Upload to Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app (or create a new app)
3. Go to **Release** → **Testing** → **Internal testing** (or **Closed testing**)
4. Click **Create new release**
5. Upload your AAB or APK file
6. Fill in release notes
7. Review and roll out to your testers

## Current App Configuration

- **Application ID**: `com.chefpro4home`
- **Version Code**: 1
- **Version Name**: 1.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Troubleshooting

### Build fails with "keystore.properties not found"
- Make sure you've created `keystore.properties` from the example file
- Verify the path to your keystore file is correct

### Build succeeds but APK/AAB is unsigned
- Check that your `keystore.properties` file has the correct values
- Verify the keystore file path is correct in `keystore.properties`

### Forgot keystore password
- Unfortunately, there's no way to recover a lost keystore password
- If you lose it, you'll need to create a new keystore and app signing key
- Google Play App Signing can help with this - enable it in Play Console

## Security Best Practices

1. **Back up your keystore file** to a secure location (encrypted)
2. **Use Google Play App Signing** - This allows Google to manage your app signing key
3. **Never commit keystore files or passwords** to version control
4. **Store credentials securely** - Consider using a password manager

## Next Steps

After uploading to closed testing:
1. Test the app thoroughly on various devices
2. Gather feedback from testers
3. Increment `versionCode` and `versionName` in `app/build.gradle.kts` for future releases
4. Consider enabling Google Play App Signing for additional security

