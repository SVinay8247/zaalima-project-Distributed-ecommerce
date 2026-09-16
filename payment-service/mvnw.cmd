@echo off
setlocal
set "BASE_DIR=%~dp0"
set "V=3.9.15"
set "HOME=%BASE_DIR%.maven\apache-maven-%V%"
set "ZIP=%BASE_DIR%.maven\apache-maven-%V%-bin.zip"
if not exist "%HOME%\bin\mvn.cmd" (
 if not exist "%BASE_DIR%.maven" mkdir "%BASE_DIR%.maven"
 if not exist "%ZIP%" powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%V%/apache-maven-%V%-bin.zip' -OutFile '%ZIP%'"
 powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%BASE_DIR%.maven'"
)
call "%HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
