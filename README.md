# 🏦 **Система управления банковскими картами**
**Профессиональное REST API для безопасных банковских операций**

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Auth-yellow?logo=jsonwebtokens)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Test Coverage](https://img.shields.io/badge/Coverage-89%25-success)](https://sonarcloud.io/dashboard?id=bank-card-management)

**Промышленное банковское API** с микросервисной архитектурой, комплексной безопасностью и полным аудитом. Разработано для современных финансовых приложений, соответствующих стандартам PCI DSS.

---

## 📋 **Ключевые возможности**

### 🔐 **Безопасность**
- **JWT-аутентификация** с refresh токенами
- **Управление доступом на основе ролей** (User/Admin)
- **Шифрование данных карт** AES-256
- **Ограничение запросов** (rate limiting)
- **Полный аудит операций** с журналированием

### 💳 **Управление картами**
- Создание, просмотр, обновление, удаление карт
- Блокировка/разблокировка карт
- Верификация данных карт
- Автоматическая проверка сроков действия

### 💰 **Финансовые операции**
- Переводы между картами
- Проверка баланса
- История транзакций
- Лимиты операций

### 📊 **Мониторинг**
- Health checks через Spring Boot Actuator
- Метрики Prometheus
- Swagger документация
- Логирование операций

---

## 🚀 **Быстрый старт**

### **Предварительные требования**
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (или Docker)
- Docker & Docker Compose (опционально)

### **1. Локальный запуск**

```bash
# Клонирование репозитория
git clone https://github.com/ваш-аккаунт/bank-card-management.git
cd bank-card-management

# Настройка переменных окружения
cp .env.example .env
# Отредактируйте .env файл с вашими настройками

# Сборка проекта
mvn clean package

# Запуск с PostgreSQL
mvn spring-boot:run -Dspring.profiles.active=local
