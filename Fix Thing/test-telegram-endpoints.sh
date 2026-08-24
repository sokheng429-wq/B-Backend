#!/bin/bash
# Test script to verify Telegram endpoints are working

echo "============================================"
echo "Testing Telegram Login Backend Endpoints"
echo "============================================"
echo ""

# Test 1: Initialize login session
echo "1. Testing POST /api/auth/telegram/init"
echo "-------------------------------------------"
INIT_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/auth/telegram/init" \
  -H "Content-Type: application/json")
echo "$INIT_RESPONSE" | python -m json.tool 2>/dev/null || echo "$INIT_RESPONSE"
echo ""

# Extract token from response
TOKEN=$(echo "$INIT_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  echo "✅ Session token: $TOKEN"
  echo ""

  # Test 2: Check status
  echo "2. Testing GET /api/auth/telegram/status/{token}"
  echo "-------------------------------------------"
  STATUS_RESPONSE=$(curl -s "http://localhost:8081/api/auth/telegram/status/$TOKEN")
  echo "$STATUS_RESPONSE" | python -m json.tool 2>/dev/null || echo "$STATUS_RESPONSE"
  echo ""

  # Test 3: Generate deep link
  echo "3. Generated Telegram deep link:"
  echo "-------------------------------------------"
  echo "https://t.me/BGroceriesbot?start=$TOKEN"
  echo ""
  echo "➡️  Open this link in Telegram and tap 'Start'"
  echo "➡️  Then run: curl http://localhost:8081/api/auth/telegram/status/$TOKEN"
  echo "➡️  Status should change from PENDING to COMPLETED with JWT"
else
  echo "❌ Failed to create session - endpoints may not be loaded"
  echo ""
  echo "Please restart your backend:"
  echo "  mvn spring-boot:run"
fi

echo ""
echo "============================================"
