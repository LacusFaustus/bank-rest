@echo off
chcp 65001 >nul
title Bank REST API - Information
echo ========================================
echo        Bank REST API Information
echo ========================================
echo.
echo 📍 Project Structure:
echo    - Source: src/main/java/com/bank/
echo    - Tests: src/test/java/com/bank/
echo    - Config: src/main/resources/
echo    - DB Migrations: src/main/resources/db/migration/
echo.
echo 🚀 Quick Start:
echo    1. Run 'run-local.bat' for development
echo    2. Run 'docker-run.bat' for Docker deployment
echo    3. Run 'run-tests.bat' to verify everything works
echo.
echo 📚 API Documentation:
echo    - Swagger UI: http://localhost:8080/swagger-ui.html
echo    - OpenAPI: http://localhost:8080/v3/api-docs
echo.
echo 🗄️  Databases:
echo    - Development: H2 (in-memory)
echo    - Production: PostgreSQL
echo    - H2 Console: http://localhost:8080/h2-console
echo.
echo 🔐 Default Users:
echo    Administrator:
echo      Username: admin
echo      Password: admin123
echo.
echo    Regular User:
echo      Username: user1
echo      Password: user123
echo.
echo 📋 Available Endpoints:
echo    - POST /api/auth/login - User authentication
echo    - GET  /api/cards - User's cards (with pagination)
echo    - POST /api/cards/transfer - Transfer between cards
echo    - POST /api/admin/cards - Create card (Admin only)
echo    - GET  /api/admin/cards - All cards (Admin only)
echo.
echo ========================================
pause
