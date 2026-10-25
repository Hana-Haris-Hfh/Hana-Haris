@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Launching StudySpace Web Server
echo ===================================================

cd /d "%~dp0"

:: Detect Java installation
if not defined JAVA_HOME (
    for /d %%i in ("C:\Program Files\Microsoft\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\java.exe" set "JAVA_HOME=%%i"
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_BIN=java.exe"
)

:: Rebuild jar if missing
if not exist "studyspace.jar" (
    echo studyspace.jar not found. Running build.bat first...
    call build.bat
    if %errorlevel% neq 0 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
)

echo Starting StudySpace Web Server at http://localhost:8080 ...
"%JAVA_BIN%" -jar "%~dp0studyspace.jar" --web --port 8080
pause
