@echo off
echo Getting SHA-1 fingerprint for debug keystore...
echo.
echo Looking for keystore at: %USERPROFILE%\.android\debug.keystore
echo.

keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

echo.
echo.
echo Copy the SHA1 value from above and add it to Firebase Console
echo.
pause
