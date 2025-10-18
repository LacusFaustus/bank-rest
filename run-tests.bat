@echo off
chcp 65001 >nul
title Bank REST API - Tests
echo ========================================
echo    Running Bank REST API Tests
echo ========================================
echo.

echo Step 1: Cleaning project...
call mvn clean -q

echo.
echo Step 2: Compiling project...
call mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation failed! Fix errors first.
    pause
    exit /b 1
)

echo.
echo Step 3: Running tests...
call mvn test

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ All tests passed successfully!
    echo.
    echo Generating test report...
    call mvn jacoco:report -q
    echo.
    echo 📊 Test report generated in: target/site/jacoco/index.html
) else (
    echo.
    echo ❌ Some tests failed!
)

echo.
pause
