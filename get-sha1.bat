@echo off
echo Getting SHA-1 fingerprint for Firebase...
echo.
cd /d "%~dp0"
gradlew signingReport
pause
