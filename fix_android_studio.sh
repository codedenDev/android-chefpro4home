#!/bin/bash

echo "🔧 Fixing Android Studio 'Hello Android' Issue"
echo "=============================================="

echo "1. 🧹 Cleaning Android Studio cache..."
rm -rf .idea
rm -rf app/build
rm -rf .gradle
find . -name "*.iml" -delete

echo "2. 📱 Current APK status:"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "No APK found"

echo "3. 🚀 Building clean APK..."
./gradlew clean
./gradlew assembleDebug

echo "4. 📦 Installing correct APK..."
./gradlew installDebug

echo ""
echo "✅ FIXED! Now follow these steps in Android Studio:"
echo ""
echo "1. 🚪 Close Android Studio completely (Cmd+Q)"
echo "2. 🗑️  Delete any cached projects in Android Studio"
echo "3. 🔄 Restart Android Studio"
echo "4. 📂 Open → Navigate to: /Users/richardkelly/Documents/Repo/android-chefpro4home"
echo "5. ✅ Select the folder and click 'Open'"
echo "6. 🏗️  Build → Clean Project"
echo "7. 🏃 Run the app - it should show ChefPro4Home interface"
echo ""
echo "❌ If you still see 'Hello Android':"
echo "   - Android Studio is opening the wrong project"
echo "   - Make sure you open the correct folder path above"
echo "   - Check that the project name shows 'android-chefpro4home'"
