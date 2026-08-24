@echo off
REM Test script to verify Telegram endpoints are working

echo ============================================
echo Testing Telegram Login Backend Endpoints
echo ============================================
echo.

REM Test 1: Initialize login session
echo 1. Testing POST /api/auth/telegram/init
echo -------------------------------------------
curl -s -X POST "http://localhost:8081/api/auth/telegram/init" -H "Content-Type: application/json" > temp_response.json
type temp_response.json
echo.
echo.

REM Parse token (basic parsing for Windows)
for /f "tokens=2 delims=:," %%a in ('findstr "token" temp_response.json') do set TOKEN_RAW=%%a
set TOKEN=%TOKEN_RAW:"=%
set TOKEN=%TOKEN: =%

if not "%TOKEN%"=="" (
  echo Token found: %TOKEN%
  echo.

  REM Test 2: Check status
  echo 2. Testing GET /api/auth/telegram/status/{token}
  echo -------------------------------------------
  curl -s "http://localhost:8081/api/auth/telegram/status/%TOKEN%"
  echo.
  echo.

  REM Test 3: Generate deep link
  echo 3. Generated Telegram deep link:
  echo -------------------------------------------
  echo https://t.me/BGroceriesbot?start=%TOKEN%
  echo.
  echo Open this link in Telegram and tap 'Start'
  echo Then check status again with: curl http://localhost:8081/api/auth/telegram/status/%TOKEN%
) else (
  echo Failed to create session - endpoints may not be loaded
  echo.
  echo Please restart your backend:
  echo   mvn spring-boot:run
)

del temp_response.json 2>nul
echo.
echo ============================================
pause
