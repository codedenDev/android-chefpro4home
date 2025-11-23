#!/bin/bash
APK="app/build/outputs/apk/release/app-release.apk"
KEYSTORE="app/release.keystore"
PASSWORD="android123"
ALIAS="release"

echo "🔐 Signing APK..."
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore "$KEYSTORE" \
  -storepass "$PASSWORD" \
  "$APK" "$ALIAS"

echo ""
echo "✅ Verifying signature..."
jarsigner -verify -verbose "$APK" | grep -E "jar verified|signed"
echo ""
echo "✅ APK signed successfully!"
