@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Running StudySpace Test Suite
echo ===================================================

where javac >nul 2>&1
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        for /d %%i in ("C:\Program Files\Microsoft\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Java\jdk*") do (
            if exist "%%i\bin\javac.exe" (
                set "JAVA_HOME=%%i"
                set "PATH=%%i\bin;%PATH%"
            )
        )
    )
)

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] javac was not found. Please ensure JDK 17+ is installed.
    exit /b 1
)

if not exist "bin" (
    call build.bat
)

echo Compiling test suite...
javac -encoding UTF-8 -cp bin -d bin "src\test\java\com\studyspace\StudyPlannerServiceTest.java"

if %errorlevel% neq 0 (
    echo [ERROR] Test compilation failed!
    exit /b %errorlevel%
)

echo Executing tests...
java -cp bin com.studyspace.StudyPlannerServiceTest
