@echo off
set "PROJECT_DIR=C:\Users\ibrag\Documents\GitHub\RavenBS-Plus-Plus"
set "MODS_DIR=C:\Users\ibrag\AppData\Roaming\.minecraft\mods"
set "JAR_NAME=raven-bs-fabric-1.0.0.jar"
set "BUILD_JAR=%PROJECT_DIR%\build\libs\%JAR_NAME%"
set "TARGET_JAR=%MODS_DIR%\%JAR_NAME%"

cd /d "%PROJECT_DIR%"
echo Building project...
call gradlew build

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Build successful!

if exist "%TARGET_JAR%" (
    echo Deleting old mod...
    del "%TARGET_JAR%"
)

echo Copying new mod...
copy "%BUILD_JAR%" "%MODS_DIR%"

echo Done!
pause
