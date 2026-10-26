@echo off
rem Builds and runs Zoo Manager.
rem   run            play the console version
rem   run gui        play the Swing (window) version
rem   run test       run the JUnit tests
rem Uses the JDK in JAVA_HOME when set, so a stray older "java" on PATH can't break it.
setlocal
cd /d "%~dp0"

set "JBIN="
if defined JAVA_HOME set "JBIN=%JAVA_HOME%\bin\"
set "JSON_JAR=lib\json-20250517.jar"
set "JUNIT_JAR=lib\junit-platform-console-standalone-1.10.2.jar"
set "OUT=out"

set "MODE=%~1"
if "%MODE%"=="" set "MODE=console"

if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

if /i "%MODE%"=="test" (
    "%JBIN%javac" --release 8 -nowarn -encoding UTF-8 -cp "%JSON_JAR%;%JUNIT_JAR%" -d "%OUT%" src\main\model\*.java src\main\persistence\*.java src\main\ui\*.java src\test\model\*.java || exit /b 1
    "%JBIN%java" -jar "%JUNIT_JAR%" execute -cp "%OUT%;%JSON_JAR%" --scan-classpath --disable-banner --details=tree
    exit /b
)

"%JBIN%javac" --release 8 -nowarn -encoding UTF-8 -cp "%JSON_JAR%" -d "%OUT%" src\main\model\*.java src\main\persistence\*.java src\main\ui\*.java || exit /b 1

if /i "%MODE%"=="gui" (
    "%JBIN%java" -cp "%OUT%;%JSON_JAR%" ui.MainGUI
) else (
    "%JBIN%java" -cp "%OUT%;%JSON_JAR%" ui.Main
)
