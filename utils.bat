@echo off
chcp 65001 >nul
title Bank REST API - Utilities
echo ========================================
echo    Bank REST API - Utility Tools
echo ========================================
echo.

:utils_menu
echo Available utilities:
echo 1. Check Database Connection
echo 2. View Application Logs
echo 3. Clean and Rebuild
echo 4. Dependency Tree
echo 5. Back to Main Menu
echo.
set /p util_choice="Select utility (1-5): "

if "%util_choice%"=="1" goto check_db
if "%util_choice%"=="2" goto view_logs
if "%util_choice%"=="3" goto clean_rebuild
if "%util_choice%"=="4" goto dep_tree
if "%util_choice%"=="5" goto exit_utils
echo Invalid choice. Please try again.
echo.
goto utils_menu

:check_db
echo.
echo Checking database connectivity...
echo For H2: jdbc:h2:mem:bankdb
echo For PostgreSQL: jdbc:postgresql://localhost:5432/bankdb
echo.
pause
goto utils_menu

:view_logs
echo.
echo Viewing application logs...
if exist logs\bank-app.log (
    type logs\bank-app.log | more
) else (
    echo No log file found. Run the application first.
)
pause
goto utils_menu

:clean_rebuild
echo.
echo Cleaning and rebuilding project...
call mvn clean compile
echo.
echo ✅ Clean and rebuild completed!
pause
goto utils_menu

:dep_tree
echo.
echo Generating dependency tree...
call mvn dependency:tree
pause
goto utils_menu

:exit_utils
exit
