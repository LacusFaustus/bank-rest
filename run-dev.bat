@echo off
chcp 65001 >nul
title Bank REST API - Development (PostgreSQL)
echo ========================================
echo    Bank REST API - Development Mode
echo ========================================
echo.
echo Starting application with PostgreSQL...
echo Make sure PostgreSQL is running on localhost:5432
echo.
echo 📊 Database:      PostgreSQL (localhost:5432/bankdb)
echo 📚 Swagger UI:    http://localhost:8080/swagger-ui.html
echo 📖 API Docs:      http://localhost:8080/v3/api-docs
echo.
echo 🔐 Test Users:
echo    Admin: admin / admin123
echo    User:  user1 / user123
echo.
echo Press Ctrl+C to stop the application
echo ========================================
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev
pause
