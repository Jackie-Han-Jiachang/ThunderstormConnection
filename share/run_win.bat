@echo off

echo Starting Thunderstorm...

start "" java -jar .\thunderstorm-1.0.0.jar

:wait
timeout /t 1 > nul

curl -s http://localhost:8080 > nul

if errorlevel 1 goto wait

echo Server started!

start "" http://localhost:8080