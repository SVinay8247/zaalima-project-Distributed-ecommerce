@echo off
setlocal
set "MVN_VERSION=3.9.16"
set "MVN_HOME=%USERPROFILE%\.m2\wrapper\apache-maven-%MVN_VERSION%"
set "MVN_BIN=%MVN_HOME%\bin\mvn.cmd"

if exist "%MVN_BIN%" (
    call "%MVN_BIN%" %*
    exit /b %ERRORLEVEL%
)

echo Maven %MVN_VERSION% is not installed locally.
echo Downloading Maven %MVN_VERSION% automatically...

set "ZIP=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force -Path '%USERPROFILE%\.m2\wrapper' | Out-Null; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%ZIP%'; Expand-Archive -Path '%ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper' -Force; Remove-Item '%ZIP%' -Force"

if not exist "%MVN_BIN%" (
    echo Failed to install Maven automatically.
    exit /b 1
)

call "%MVN_BIN%" %*
exit /b %ERRORLEVEL%
