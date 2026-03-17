@echo off
setlocal

set "ROOT_DIR=%~dp0"

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    goto java_ready
  )
)

where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Java not found. Please configure JAVA_HOME or add java to PATH.
  exit /b 1
)

:java_ready

if "%DEEPSEEK_API_KEY%"=="" (
  echo [WARN] DEEPSEEK_API_KEY is empty, AI chat will return config prompt.
)

echo [INFO] Starting backend on http://127.0.0.1:8080
echo [INFO] Knife4j docs: http://127.0.0.1:8080/doc.html

pushd "%ROOT_DIR%backend"
call mvn -DskipTests spring-boot:run
popd

endlocal
