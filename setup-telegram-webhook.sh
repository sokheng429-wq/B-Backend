#!/bin/bash

# Telegram Bot Webhook Setup Script for B'Groceries
# Using localtunnel: https://shaggy-socks-shout.loca.lt

BOT_TOKEN="8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU"
WEBHOOK_URL="https://shaggy-socks-shout.loca.lt/api/telegram/webhook"
WEBHOOK_SECRET=$(openssl rand -hex 32 2>/dev/null || echo "")

echo "============================================"
echo "Telegram Bot Webhook Setup"
echo "============================================"
echo ""
echo "Bot: @BGroceriesbot"
echo "Webhook URL: $WEBHOOK_URL"
echo ""

# Generate webhook secret if openssl is available
if [ -z "$WEBHOOK_SECRET" ]; then
    echo "⚠️  OpenSSL not found - skipping webhook secret (optional but recommended)"
    echo ""

    # Set webhook without secret
    echo "Setting webhook..."
    curl -X POST "https://api.telegram.org/bot${BOT_TOKEN}/setWebhook" \
      -H "Content-Type: application/json" \
      -d "{\"url\": \"${WEBHOOK_URL}\"}"
else
    echo "🔐 Generated webhook secret: $WEBHOOK_SECRET"
    echo ""
    echo "⚠️  IMPORTANT: Add this to your application.yml or environment:"
    echo "   telegram.webhook.secret: $WEBHOOK_SECRET"
    echo "   OR"
    echo "   export TELEGRAM_WEBHOOK_SECRET=$WEBHOOK_SECRET"
    echo ""

    # Set webhook with secret
    echo "Setting webhook with security token..."
    curl -X POST "https://api.telegram.org/bot${BOT_TOKEN}/setWebhook" \
      -H "Content-Type: application/json" \
      -d "{\"url\": \"${WEBHOOK_URL}\", \"secret_token\": \"${WEBHOOK_SECRET}\"}"
fi

echo ""
echo ""
echo "============================================"
echo "Verifying webhook..."
echo "============================================"
sleep 2
curl "https://api.telegram.org/bot${BOT_TOKEN}/getWebhookInfo" | python -m json.tool 2>/dev/null || curl "https://api.telegram.org/bot${BOT_TOKEN}/getWebhookInfo"

echo ""
echo ""
echo "============================================"
echo "✅ Setup Complete!"
echo "============================================"
echo ""
echo "Next steps:"
echo "1. Make sure your backend is running on port 8081"
echo "2. Make sure localtunnel is running: lt --port 8081 --subdomain shaggy-socks-shout"
echo "3. Test by opening: https://t.me/BGroceriesbot"
echo "4. Type /start in the bot chat to verify webhook receives messages"
echo ""
echo "To delete webhook (if needed):"
echo "  curl -X POST \"https://api.telegram.org/bot${BOT_TOKEN}/deleteWebhook\""
echo ""
