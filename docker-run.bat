@echo off
chcp 65001 >nul
title Bank REST API - Docker
echo ========================================
echo    Bank REST API - Docker Deployment
echo ========================================
echo.

echo Stopping any running containers...
call docker-compose down

echo.
echo Building and starting containers...
echo.
call docker-compose up --build

echo.
echo 🐳 Application is running in Docker containers
echo 📚 Swagger UI: http://localhost:8080/swagger-ui.html
echo 📊 PostgreSQL: localhost:5432
echo.
echo Press Ctrl+C to stop containers
echo.

pause
