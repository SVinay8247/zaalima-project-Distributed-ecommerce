@echo off
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  mvn %*
  exit /b %ERRORLEVEL%
)
echo Maven is not available on PATH. Please install Maven or run the application from VS Code/Eclipse.
exit /b 1
