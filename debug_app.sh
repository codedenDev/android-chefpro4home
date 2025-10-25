#!/bin/bash

echo "🔍 ChefPro4Home App Diagnostic Script"
echo "======================================"

# Check if ADB is available
if command -v adb &> /dev/null; then
    echo "✅ ADB is available"
    
    # Check connected devices
    echo "📱 Connected devices:"
    adb devices
    
    # Check if app is installed
    echo "📦 App installation status:"
    adb shell pm list packages | grep chefpro4home
    
    # Get app logs
    echo "📋 Recent app logs:"
    adb logcat -d | grep -E "(ChefPro4Home|AndroidRuntime|FATAL|ERROR)" | tail -20
    
    # Try to start the app
    echo "🚀 Attempting to start app:"
    adb shell am start -n com.chefpro4home/.MainActivity
    
    # Check if app is running
    echo "🔄 App running status:"
    adb shell ps | grep chefpro4home
    
else
    echo "❌ ADB not available - cannot run device diagnostics"
    echo "💡 Please ensure Android SDK is installed and ADB is in PATH"
fi

echo ""
echo "🔧 Manual troubleshooting steps:"
echo "1. Check if the app appears in your device's app drawer"
echo "2. Try launching the app manually from the device"
echo "3. Check device storage space"
echo "4. Restart the device if needed"
echo "5. Check if the app crashes on startup (look for error messages)"
