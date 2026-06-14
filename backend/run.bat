@echo off
setlocal enabledelayedexpansion

REM run.bat
REM
REM Runs the Ridex Spring Boot app WITHOUT requiring a system-wide Maven
REM installation. On first run, it downloads a local copy of Apache Maven
REM into .\.maven (no admin rights, no PATH changes) and uses that.
REM
REM Requirements: Java 17+ must already be installed (check with `java -version`).
REM
REM Usage:
REM   run.bat

set MAVEN_VERSION=3.9.6
set MAVEN_DIR=%cd%\.maven
set MAVEN_HOME=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%
set MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd

if not exist "%MAVEN_BIN%" (
    echo Maven not found locally - downloading Apache Maven %MAVEN_VERSION% ^(one-time setup^)...
    if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"

    set "DOWNLOAD_URL=https://archive.apache.org/dist/maven/maven-3/!MAVEN_VERSION!/binaries/apache-maven-!MAVEN_VERSION!-bin.zip"
    set "ARCHIVE_PATH=!MAVEN_DIR!\maven.zip"

    echo Downloading from !DOWNLOAD_URL! ...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri '!DOWNLOAD_URL!' -OutFile '!ARCHIVE_PATH!'"

    if not exist "!ARCHIVE_PATH!" (
        echo ERROR: Download failed. Check your internet connection and try again.
        exit /b 1
    )

    echo Extracting Maven...
    powershell -NoProfile -Command "Expand-Archive -Path '!ARCHIVE_PATH!' -DestinationPath '!MAVEN_DIR!' -Force"
    del "!ARCHIVE_PATH!"

    echo Maven downloaded to !MAVEN_HOME!
)

echo Starting Ridex (Spring Boot)...
call "%MAVEN_BIN%" spring-boot:run
