# Bank REST API 🏦

A secure Spring Boot REST API for bank card management with JWT authentication, role-based access control, and comprehensive monitoring.

## 🚀 Features

- **JWT Authentication** - Secure token-based authentication
- **Card Management** - Create, view, update, and delete bank cards
- **Money Transfers** - Transfer funds between user cards
- **Role-Based Access** - USER and ADMIN roles with different permissions
- **Data Encryption** - Card numbers encrypted in database
- **Audit Logging** - Comprehensive activity tracking
- **Rate Limiting** - Protection against abuse
- **OpenAPI Documentation** - Interactive API documentation
- **Monitoring** - Health checks and metrics

## 🛠 Tech Stack

- **Java 17** - Core programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **JWT** - JSON Web Tokens for security
- **H2/PostgreSQL** - Database (H2 for development, PostgreSQL for production)
- **JPA/Hibernate** - ORM and data persistence
- **Liquibase** - Database migration tool
- **OpenAPI 3** - API documentation
- **Maven** - Dependency management

## 📋 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/validate` - Validate JWT token

### User Card Management
- `GET /api/cards` - Get user's cards
- `GET /api/cards/{id}` - Get card details
- `POST /api/cards/transfer` - Transfer between cards
- `POST /api/cards/{id}/block-request` - Request card block

### Admin Operations
- `GET /api/admin/cards` - Get all cards (Admin only)
- `POST /api/admin/cards` - Create new card (Admin only)
- `PUT /api/admin/cards/{id}/status` - Update card status (Admin only)
- `DELETE /api/admin/cards/{id}` - Delete card (Admin only)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- (Optional) Docker and Docker Compose

### Local Development
```bash
# Clone the repository
git clone https://github.com/LacusFaustus/bank-rest.git
cd bank-rest

# Run with Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Or build and run
./mvnw clean package
java -jar target/bank-rest-1.0.0.jar
