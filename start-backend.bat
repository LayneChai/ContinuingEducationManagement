@echo off
setlocal

set "ROOT_DIR=%~dp0"
set "JDK_DIR=%ROOT_DIR%.tools\jdk-17.0.18+8"

if not exist "%JDK_DIR%\bin\java.exe" (
  echo [ERROR] JDK 17 not found: "%JDK_DIR%"
  exit /b 1
)

set "JAVA_HOME=%JDK_DIR%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if "%DEEPSEEK_API_KEY%"=="" (
  echo [WARN] DEEPSEEK_API_KEY is empty, AI chat will return config prompt.
)

echo [INFO] Starting backend on http://127.0.0.1:8080
echo [INFO] Knife4j docs: http://127.0.0.1:8080/doc.html

pushd "%ROOT_DIR%backend"
call mvn -DskipTests spring-boot:run
popd

endlocal
