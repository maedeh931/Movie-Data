@echo off
REM ======================================================================
REM Run Movie Watchlist Manager
REM ======================================================================

set "JAVA_HOME=C:\Users\Madiha_Rahman\.jdks\openjdk-26.0.2"
set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

echo Starting CineVault — Modern Movie Tracker...
"%MVN_CMD%" javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application failed to start. Press any key to exit...
    pause
)
