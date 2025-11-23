#!/bin/bash

echo "🔧 AGGRESSIVE SDK Platform 35 Fix"
echo "=================================="
echo ""

SDK_DIR="$HOME/Library/Android/sdk"
PLATFORM_DIR="$SDK_DIR/platforms/android-35"

echo "Step 1: Removing incomplete SDK Platform 35..."
rm -rf "$PLATFORM_DIR"
echo "✅ Removed $PLATFORM_DIR"
echo ""

echo "Step 2: Clearing Android Studio caches..."
rm -rf "$HOME/.android/cache" 2>/dev/null
echo "✅ Cleared Android Studio cache"
echo ""

echo "Step 3: Checking SDK directory permissions..."
ls -ld "$SDK_DIR/platforms" | awk '{print "Permissions: " $1 " Owner: " $3 ":" $4}'
echo ""

echo "📋 NEXT STEPS (Manual):"
echo ""
echo "1. Close Android Studio completely"
echo "2. Open Android Studio"
echo "3. Tools → SDK Manager"
echo "4. SDK Platforms tab"
echo "5. CHECK 'Android 15.0 (API 35)'"
echo "6. Click Apply"
echo "7. Wait for complete installation"
echo ""
echo "8. Verify installation:"
echo "   ls -lh $PLATFORM_DIR/android.jar"
echo ""
echo "The file should be ~5-10 MB in size."
echo ""
echo "If this still doesn't work, there may be:"
echo "- Network/firewall issues blocking downloads"
echo "- Disk space issues"
echo "- Android Studio installation problems"
echo ""

