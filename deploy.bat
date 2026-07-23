@echo off
set "PROJECT_DIR=C:\Users\ibrag\Documents\GitHub\RavenBS-Plus-Plus"
set "MODS_DIR=C:\Users\ibrag\AppData\Roaming\.sectlauncher\instances\c5ac291e-5272-4d8e-ac57-30e47d011a2a\mods"

cd /d "%PROJECT_DIR%"
echo Building project...
rem remapJar produces the Fabric-ready JAR without running test tasks.
call gradlew clean remapJar --no-daemon

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Build successful!

if not exist "%MODS_DIR%" (
    mkdir "%MODS_DIR%"
)

echo Deleting old RavenBS versions...
if exist "%MODS_DIR%\raven-bs-fabric-*.jar" (
    del "%MODS_DIR%\raven-bs-fabric-*.jar"
)

set "MOD_JAR="
for /f "delims=" %%F in ('dir /b /a-d "%PROJECT_DIR%\build\libs\raven-bs-fabric-*.jar" ^| findstr /v /i /c:"-sources.jar" /c:"-dev.jar"') do set "MOD_JAR=%PROJECT_DIR%\build\libs\%%F"

if not defined MOD_JAR (
    echo Release jar not found in "%PROJECT_DIR%\build\libs"
    pause
    exit /b 1
)

echo Copying release mod jar...
copy /y "%MOD_JAR%" "%MODS_DIR%\" >nul

echo Done!
if not defined RAVEN_DEPLOY_NO_PAUSE pause
