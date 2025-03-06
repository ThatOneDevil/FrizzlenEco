@echo off
echo Building FrizzlenEco...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)
echo Build successful!
echo JAR file is in the target directory.
pause 