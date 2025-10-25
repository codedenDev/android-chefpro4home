#!/bin/bash

echo "📱 Checking Android app logs for API calls..."

# Check if adb is available
if command -v adb &> /dev/null; then
    echo "🔍 Looking for API-related logs..."
    adb logcat -d | grep -E "(🌐|📋|✅|❌|💥|Cloudflare|API)" | tail -20
else
    echo "⚠️ ADB not available. Please check Android Studio logs or device logs manually."
    echo "Look for logs containing: 🌐, 📋, ✅, ❌, 💥, Cloudflare, API"
fi

echo "✅ Log check complete!"
