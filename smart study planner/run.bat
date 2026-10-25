@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Launching StudySpace (Desktop GUI + Server)
echo ===================================================

cd /d "%~dp0"

:: Detect Java installation
if not defined JAVA_HOME (
    for /d %%i in ("C:\Program Files\Microsoft\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\javaw.exe" set "JAVA_HOME=%%i"
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    set "JAVA_BIN=%JAVA_HOME%\bin\javaw.exe"
) else (
    set "JAVA_BIN=javaw.exe"
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

echo Starting StudySpace...
start "" "%JAVA_BIN%" -jar "%~dp0studyspace.jar"
echo StudySpace Desktop application is now running!
