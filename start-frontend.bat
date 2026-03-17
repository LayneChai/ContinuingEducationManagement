@echo off
setlocal

set "ROOT_DIR=%~dp0"

echo [INFO] Starting frontend on http://127.0.0.1:5173
pushd "%ROOT_DIR%frontend"
call npm run dev -- --host 0.0.0.0 --port 5173
popd

endlocal
