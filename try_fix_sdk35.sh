#!/bin/bash
echo "🔧 Attempting to fix SDK Platform 35 installation..."
echo ""

SDK_DIR="$HOME/Library/Android/sdk/platforms/android-35"

# Try to use Android SDK manager if available
SDKMANAGER=$(find "$HOME/Library/Android/sdk" -name "sdkmanager" -type f 2>/dev/null | head -1)

if [ -n "$SDKMANAGER" ]; then
    echo "Found sdkmanager at: $SDKMANAGER"
    echo ""
    echo "Attempting to install SDK Platform 35..."
    export JAVA_HOME=$(/usr/libexec/java_home)
    export ANDROID_HOME="$HOME/Library/Android/sdk"
    yes | "$SDKMANAGER" "platforms;android-35" 2>&1 | tail -20
else
    echo "❌ sdkmanager not found"
    echo "Please install SDK Platform 35 via Android Studio SDK Manager"
fi

echo ""
echo "Checking if android.jar now exists..."
if [ -f "$SDK_DIR/android.jar" ]; then
    echo "✅ android.jar found!"
    ls -lh "$SDK_DIR/android.jar"
else
    echo "❌ android.jar still missing"
fi
