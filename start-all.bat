@echo off
setlocal

set "ROOT_DIR=%~dp0"

start "Continue Education Backend" cmd /k ""%ROOT_DIR%start-backend.bat""
timeout /t 3 /nobreak >nul
start "Continue Education Frontend" cmd /k ""%ROOT_DIR%start-frontend.bat""

echo [INFO] Backend and frontend launch commands sent.
echo [INFO] Backend:  http://127.0.0.1:8080
echo [INFO] Frontend: http://127.0.0.1:5173

endlocal
