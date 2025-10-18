@echo off
chcp 65001 >nul
title Bank REST API - Build and Run
echo ========================================
echo    Bank REST API - Complete Build & Run
echo ========================================
echo.

echo Step 1: Cleaning project...
call mvn clean -q

echo.
echo Step 2: Compiling project...
call mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation failed! Please fix compilation errors first.
    pause
    exit /b 1
)

echo.
echo Step 3: Running tests...
call mvn test -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ⚠️  Some tests failed, but continuing with build...
)

echo.
echo Step 4: Packaging application...
call mvn package -q -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo.
echo Step 5: Starting application...
echo.
echo ✅ Build successful! Starting application...
echo 📚 Swagger UI: http://localhost:8080/swagger-ui.html
echo.

java -jar target/bank-rest-1.0.0.jar --spring.profiles.active=local

pause
