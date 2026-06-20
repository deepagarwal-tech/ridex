@echo off
REM start.bat
REM
REM Starts both the Ridex backend and frontend with one command.
REM Opens each in its own window so you can see their logs separately.
REM
REM Usage (from the ridex-app folder):
REM   start.bat

echo Starting Ridex backend...
start "Ridex Backend" cmd /k "cd /d %~dp0backend && run.bat"

echo Waiting a few seconds for the backend to begin starting...
timeout /t 5 /nobreak >nul

echo Starting Ridex frontend...
start "Ridex Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo Both processes are starting in their own windows.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.
echo You can close this window now - the other two will keep running.
