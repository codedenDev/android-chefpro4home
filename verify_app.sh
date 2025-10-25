#!/bin/bash

echo "🔍 ChefPro4Home App Verification"
echo "=================================="

# Check if ADB is available
if command -v adb &> /dev/null; then
    echo "✅ ADB is available"
    
    # Check connected devices
    echo "📱 Connected devices:"
    adb devices
    
    # Check if ChefPro4Home app is installed
    echo "📦 ChefPro4Home app installation status:"
    adb shell pm list packages | grep chefpro4home
    
    # Get app info
    echo "📋 App information:"
    adb shell dumpsys package com.chefpro4home | grep -E "(versionName|versionCode|applicationLabel)"
    
    # Try to start the app
    echo "🚀 Starting ChefPro4Home app:"
    adb shell am start -n com.chefpro4home/.MainActivity
    
    echo ""
    echo "✅ If you see 'Chef Pro 4 Home' app with recipe interface, the app is correct!"
    echo "❌ If you see 'Hello Android' or template app, Android Studio is running wrong project"
    
else
    echo "❌ ADB not available - cannot run device verification"
    echo "💡 Please ensure Android SDK is installed and ADB is in PATH"
fi

echo ""
echo "🔧 Android Studio Troubleshooting:"
echo "1. Close Android Studio completely"
echo "2. Open Android Studio"
echo "3. Choose 'Open an Existing Project'"
echo "4. Navigate to: /Users/richardkelly/Documents/Repo/android-chefpro4home"
echo "5. Select the folder and click 'Open'"
echo "6. Make sure you see 'android-chefpro4home' as the project name"
echo "7. Run the app - it should show 'Chef Pro 4 Home' interface"
