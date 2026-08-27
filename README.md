# FinBank — Banking Management System

FinBank is a backend banking management system built with Java and Spring Boot. It provides secure customer authentication, account management, KYC workflows, and financial transaction processing through RESTful APIs.

The project follows a layered architecture with separate controllers, services, repositories, entities, DTOs, security components, and exception handling.

## Features

### Authentication & Authorization
- Customer login using email and password
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based authorization using Spring Security
- Protected customer and administrative endpoints

### Customer Management
- Customer registration
- Customer profile retrieval
- Customer profile updates
- Paginated customer listing
- Duplicate email validation

### Account Management
- Create bank accounts for customers
- Unique FinBank account number generation
- Account type and currency support
- Account status management
- Customer-specific account access
- Paginated account listing

### Transactions
- Deposit funds
- Withdraw funds
- Transfer funds between accounts
- Balance validation
- Same-account transfer prevention
- Transaction status and type tracking
- Unique transaction references
- Account transaction history
- Transaction lookup by reference
- Transaction timestamps

### KYC Management

- Customer KYC document submission with document type and document number
- Customer ownership verification before accessing or submitting KYC information
- KYC lifecycle tracking using `PENDING`, `UNDER_REVIEW`, `APPROVED`, and `REJECTED` states
- Prevents duplicate submissions while a KYC application is under review
- Prevents modification after KYC approval
- Admin-only access to pending KYC applications
- Admin approval and rejection workflows
- Rejection reason tracking
- KYC submission and review timestamps
- Paginated pending-KYC retrieval

#### KYC API

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/admin/kyc` | ADMIN |
| PUT | `/api/admin/kyc/{customerId}/approve` | ADMIN |
| PUT | `/api/admin/kyc/{customerId}/reject` | ADMIN |

### API & Error Handling
- RESTful API architecture
- Request validation using Jakarta Bean Validation
- DTO-based request/response handling
- Centralized exception handling
- Structured error responses
- Pagination support

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Backend framework |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data persistence |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Dependency management |
| Postman | API testing |
| JUnit | Testing |

## Architecture

The application follows a layered architecture:

```text
Client
   │
   ▼
Controller Layer
   │
   ▼
Service Layer
   │
   ▼
Repository Layer
   │
   ▼
MySQL Database

Security is handled through Spring Security and a custom JWT authentication filter.

HTTP Request
     │
     ▼
JWT Authentication Filter
     │
     ▼
Spring Security
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
Database
Project Structure
src
└── main
    ├── java/com/finbank
    │   ├── config
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── exception
    │   ├── repository
    │   ├── security
    │   └── service
    │
    └── resources
        └── application.properties
API Overview
Authentication
POST /api/auth/login
Customers
POST /api/customers
GET  /api/customers/{id}
GET  /api/customers
PUT  /api/customers/{id}
Accounts
POST /api/customers/{customerId}/accounts
GET  /api/accounts/{accountNumber}
GET  /api/accounts
Transactions
POST /api/accounts/{accountNumber}/deposit
POST /api/accounts/{accountNumber}/withdraw
POST /api/accounts/{sourceAccountNumber}/transfer

GET /api/accounts/{accountNumber}/transactions
GET /api/accounts/transactions/{reference}
KYC
POST /api/...
PUT  /api/...

KYC endpoint details are intentionally kept aligned with the controller implementation and can be expanded as the API evolves.

Running Locally
Prerequisites
Java 21
Maven
MySQL 8+
IntelliJ IDEA or another Java IDE
Database

Create a MySQL database:

CREATE DATABASE finbank;
Configuration

Create:

src/main/resources/application-local.properties

and keep environment-specific credentials in that file.

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/finbank
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

jwt.secret=YOUR_JWT_SECRET
jwt.expiration=3600000

Do not commit application-local.properties to version control.

Run the Application

Using Maven:

./mvnw spring-boot:run

On Windows:

.\mvnw.cmd spring-boot:run

The application runs on:

http://localhost:8080
Testing

The project includes automated tests and REST API testing can also be performed using Postman or IntelliJ HTTP Client.

Example transaction endpoint:

POST http://localhost:8080/api/accounts/{accountNumber}/deposit
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Security

The application implements:

JWT authentication
BCrypt password hashing
Role-based endpoint authorization
Protected account and transaction operations
Environment-specific configuration
Centralized authentication error handling
Input validation

Sensitive configuration files are excluded from Git using .gitignore.

Design Principles

The project emphasizes:

Separation of concerns
Layered architecture
DTO-based API contracts
Repository abstraction
Centralized exception handling
Secure authentication
Transactional financial operations
Validation at API boundaries
Maintainable and extensible code structure
Future Improvements

Potential extensions include:

React-based banking dashboard
Advanced audit logging
Refresh-token authentication
Account statements and downloadable reports
Transaction search and filtering
Notification system
AI-powered financial assistance
Docker containerization
CI/CD pipeline
Production database configuration