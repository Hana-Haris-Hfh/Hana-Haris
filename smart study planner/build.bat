@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Building StudySpace - Smart Study Planner
echo ===================================================

:: Check Java compiler
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
    echo [ERROR] javac was not found. Please install JDK or set JAVA_HOME.
    exit /b 1
)

echo Using Java compiler:
javac -version

:: Create build directories
if not exist "bin" mkdir "bin"
if not exist "bin\web" mkdir "bin\web"

:: Compile Java sources
echo Compiling Java sources...
javac -encoding UTF-8 -d bin ^
    src/main/java/com/studyspace/*.java ^
    src/main/java/com/studyspace/model/*.java ^
    src/main/java/com/studyspace/repository/*.java ^
    src/main/java/com/studyspace/service/*.java ^
    src/main/java/com/studyspace/util/*.java ^
    src/main/java/com/studyspace/gui/*.java ^
    src/main/java/com/studyspace/gui/components/*.java ^
    src/main/java/com/studyspace/gui/dialog/*.java ^
    src/main/java/com/studyspace/gui/theme/*.java ^
    src/main/java/com/studyspace/web/*.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %errorlevel%
)

:: Copy web resources
echo Copying web resources...
xcopy /y /e /i "src\main\resources\web" "bin\web" >nul

:: Build executable JAR
echo Packaging studyspace.jar...
jar --create --file studyspace.jar --main-class com.studyspace.Main -C bin .

if %errorlevel% neq 0 (
    echo [ERROR] Packaging failed!
    exit /b %errorlevel%
)

echo ===================================================
echo   Build Successful! Generated: studyspace.jar
echo   Run with: run.bat or run-web.bat
echo ===================================================
