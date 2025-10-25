#!/bin/bash

echo "🧪 Testing Android API connection simulation..."

echo "1. Testing with Android User-Agent:"
curl -s -H "User-Agent: ChefPro4Home-Android/1.0" \
     -H "Accept: application/json" \
     -H "Content-Type: application/json" \
     "https://recipes-api.recipedos.workers.dev/api/recipes" | head -5

echo -e "\n2. Testing health endpoint:"
curl -s -H "User-Agent: ChefPro4Home-Android/1.0" \
     "https://recipes-api.recipedos.workers.dev/api/health"

echo -e "\n3. Testing with verbose output:"
curl -v -H "User-Agent: ChefPro4Home-Android/1.0" \
     "https://recipes-api.recipedos.workers.dev/api/recipes" 2>&1 | head -20

echo -e "\n✅ Android API simulation complete!"
