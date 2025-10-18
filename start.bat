@echo off
chcp 65001 >nul
title Bank REST API Launcher
echo ========================================
echo    Bank REST Application Launcher
echo ========================================
echo.

:menu
echo Available profiles:
echo 1. Local (H2 Database) - Development
echo 2. Dev (PostgreSQL) - Development
echo 3. Test - Testing
echo 4. Build and Run with Docker
echo 5. Run Tests
echo 6. Exit
echo.
set /p choice="Select option (1-6): "

if "%choice%"=="1" goto local
if "%choice%"=="2" goto dev
if "%choice%"=="3" goto test
if "%choice%"=="4" goto docker
if "%choice%"=="5" goto tests
if "%choice%"=="6" goto exit
echo Invalid choice. Please try again.
echo.
goto menu

:local
echo.
echo Starting Bank Application with LOCAL profile...
echo H2 Console: http://localhost:8080/h2-console
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo API Docs: http://localhost:8080/v3/api-docs
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=local
goto menu

:dev
echo.
echo Starting Bank Application with DEV profile...
echo Make sure PostgreSQL is running on localhost:5432
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=dev
goto menu

:test
echo.
echo Starting Bank Application with TEST profile...
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=test
goto menu

:docker
echo.
echo Building and running with Docker Compose...
echo.
call docker-compose down
call docker-compose up --build
goto menu

:tests
echo.
echo Running tests...
echo.
call mvn clean test
echo.
pause
goto menu

:exit
echo.
echo Thank you for using Bank REST API!
pause
exit
