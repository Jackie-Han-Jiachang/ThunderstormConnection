@echo off
setlocal
title Thunderstorm

rem Always run from the project folder, even when this file is double-clicked.
cd /d "%~dp0"

set "APP_URL=http://localhost:8080"
set "JAR_FILE=target\thunderstorm-1.0.0.jar"

where java >nul 2>&1
if errorlevel 1 (
    echo Java 17 or newer is required to run Thunderstorm.
    echo Install Java, then double-click run.bat again.
    echo.
    pause
    exit /b 1
)

rem If Thunderstorm is already running, just open it in the browser.
powershell.exe -NoProfile -Command "try { Invoke-WebRequest -UseBasicParsing -Uri '%APP_URL%' -TimeoutSec 2 | Out-Null; exit 0 } catch { exit 1 }"
if not errorlevel 1 (
    start "" "%APP_URL%"
    exit /b 0
)

echo Preparing Thunderstorm...
call mvnw.cmd --quiet -DskipTests package
if errorlevel 1 (
    echo.
    echo Thunderstorm could not be built.
    pause
    exit /b 1
)

echo Starting Thunderstorm...
echo Keep this window open while you use the application.
echo.

rem Wait for the server in the background, then open the default browser.
start "" /b powershell.exe -NoProfile -WindowStyle Hidden -Command "$url = '%APP_URL%'; for ($i = 0; $i -lt 60; $i++) { try { Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 2 | Out-Null; Start-Process $url; exit } catch { Start-Sleep -Seconds 1 } }"

java -jar "%JAR_FILE%"
set "APP_EXIT_CODE=%ERRORLEVEL%"

if not "%APP_EXIT_CODE%"=="0" (
    echo.
    echo Thunderstorm stopped with an error.
    pause
)

exit /b %APP_EXIT_CODE%
