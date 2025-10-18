@echo off
chcp 65001 >nul
title Bank REST API - Local Development
echo ========================================
echo    Bank REST API - Local Development
echo ========================================
echo.
echo Starting application with H2 database...
echo.
echo 📊 H2 Console:    http://localhost:8080/h2-console
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

mvn spring-boot:run -Dspring-boot.run.profiles=local
pause
