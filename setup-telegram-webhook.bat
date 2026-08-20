@echo off
REM Telegram Bot Webhook Setup Script for B'Groceries (Windows)
REM Using localtunnel: https://shaggy-socks-shout.loca.lt

set BOT_TOKEN=8953064860:AAECU4pZPZaOMZ2SInNSotSNbZA9V8M-KWU
set WEBHOOK_URL=https://shaggy-socks-shout.loca.lt/api/telegram/webhook

echo ============================================
echo Telegram Bot Webhook Setup
echo ============================================
echo.
echo Bot: @BGroceriesbot
echo Webhook URL: %WEBHOOK_URL%
echo.
echo Setting webhook...
echo.

curl -X POST "https://api.telegram.org/bot%BOT_TOKEN%/setWebhook" -H "Content-Type: application/json" -d "{\"url\": \"%WEBHOOK_URL%\"}"

echo.
echo.
echo ============================================
echo Verifying webhook...
echo ============================================
timeout /t 2 /nobreak >nul
curl "https://api.telegram.org/bot%BOT_TOKEN%/getWebhookInfo"

echo.
echo.
echo ============================================
echo Setup Complete!
echo ============================================
echo.
echo Next steps:
echo 1. Make sure your backend is running on port 8081
echo 2. Make sure localtunnel is running: lt --port 8081 --subdomain shaggy-socks-shout
echo 3. Test by opening: https://t.me/BGroceriesbot
echo 4. Type /start in the bot chat to verify webhook receives messages
echo.
echo To delete webhook (if needed):
echo   curl -X POST "https://api.telegram.org/bot%BOT_TOKEN%/deleteWebhook"
echo.
pause
