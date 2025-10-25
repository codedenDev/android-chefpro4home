#!/bin/bash

echo "🧪 Testing Cloudflare API connection..."

echo "1. Testing health endpoint:"
curl -s "https://recipes-api.recipedos.workers.dev/api/health" | head -5

echo -e "\n2. Testing recipes endpoint (first 3 recipes):"
curl -s "https://recipes-api.recipedos.workers.dev/api/recipes" | head -20

echo -e "\n3. Testing with User-Agent header:"
curl -s -H "User-Agent: ChefPro4Home-Android/1.0" "https://recipes-api.recipedos.workers.dev/api/recipes" | head -10

echo -e "\n✅ API test complete!"
