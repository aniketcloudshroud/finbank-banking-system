FinBank — Banking Management System

FinBank is a secure backend banking management system built with Java and Spring Boot. It provides customer authentication, account management, financial transaction processing, role-based authorization, transaction history and filtering, and an AI-powered financial assistant through RESTful APIs.

The application follows a layered architecture with separate controllers, services, repositories, entities, DTOs, security components, exception handling, and AI components.

The backend is designed around authenticated customer ownership, ensuring that customers can only access their own accounts, transactions, and financial information.

Features
Authentication & Authorization
Customer registration using email and password
Customer login using email and password
JWT-based stateless authentication
BCrypt password hashing
Role-based authorization using Spring Security
CUSTOMER and ADMIN roles
Protected customer and administrative endpoints
Custom JWT authentication filter
Centralized 401 Unauthorized and 403 Forbidden handling
Customer ownership checks for protected resources
Prevention of cross-customer account and transaction access
Customer Management
Customer registration
Automatic user creation during registration
Password hashing using BCrypt
Email normalization
Duplicate email validation
Customer profile retrieval
Authenticated customer profile retrieval
Customer profile updates
Paginated customer listing
Customer ownership validation
Separation between customer and user authentication data
Account Management
Create bank accounts for authenticated customers
Unique FinBank account number generation
FIN-prefixed account numbers
Account type support
Currency support
Account status management
Customer-specific account access
Paginated customer account retrieval
Paginated administrative account retrieval
Account ownership enforcement
Protection against creating accounts for another customer
Financial Transactions

FinBank supports the core banking operations required to manage customer funds.

Deposits
Deposit funds into an authenticated customer's account
Account ownership verification
Active-account validation
Balance updates
Transaction creation
Unique transaction reference generation
Transaction status tracking
Transaction timestamps
Withdrawals
Withdraw funds from an authenticated customer's account
Account ownership verification
Active-account validation
Insufficient-balance validation
Balance updates
Transaction creation
Unique transaction reference generation
Transfers
Transfer funds between FinBank accounts
Source account ownership verification
Destination account validation
Active-account validation for both accounts
Insufficient-balance validation
Same-account transfer prevention
Atomic balance updates using transactional service methods
Transaction creation with source and destination accounts
Unique transaction reference generation

Financial transaction operations use database transactions and pessimistic account locking to provide safer concurrent balance updates.

Transaction History & Filtering

FinBank provides customer-specific transaction history with filtering and pagination.

Transaction history can be filtered using:

Transaction type
From date
To date
Minimum amount
Maximum amount
Description/reference search
Pagination
Sorting by transaction creation time

Supported search fields include:

Transaction description
Transaction reference

The backend validates filter ranges, including:

fromDate cannot be after toDate
minAmount cannot be greater than maxAmount
Minimum amount cannot be negative
Maximum amount cannot be negative

Customers can only retrieve transactions associated with accounts they own.

AI-Powered Financial Assistant

FinBank includes an AI-powered financial assistant built using Spring AI and Google Gemini.

The assistant is designed as a read-only financial information assistant.

It can help authenticated customers understand information about their FinBank accounts and transaction activity without directly modifying financial data.

AI Capabilities

The assistant can:

Retrieve the authenticated customer's accounts
Retrieve account balances
Retrieve account information
Retrieve recent transactions
Retrieve transaction information using a transaction reference
Explain transaction activity
Answer general financial education questions
Perform calculations using data retrieved from the application's financial tools
Maintain conversations across multiple requests
AI Tool Calling

The financial assistant uses Spring AI tools to retrieve application data instead of allowing the language model to directly access the database.

Available financial tools include:

Tool	Purpose
getMyAccounts	Retrieves the authenticated customer's accounts
getMyRecentTransactions	Retrieves recent transactions
getMyAccount	Retrieves one account belonging to the authenticated customer
getMyTransaction	Retrieves one transaction belonging to the authenticated customer

The tools derive the customer identity from the authenticated security context rather than accepting a customer ID from the user.

This prevents the AI assistant from using client-supplied customer identifiers to access another customer's financial information.

AI Security

The AI assistant implements multiple security controls:

Customer authentication required
Customer-only access
Financial information retrieved through backend tools
Tools use the authenticated customer ID
Account ownership verification
Transaction ownership verification
Client-supplied customer IDs are not trusted
Recent transaction limits are clamped between 1 and 50
Conversation IDs are scoped to the authenticated customer
Different customers cannot share the same conversation-memory namespace
The assistant cannot execute deposits, withdrawals, or transfers
The assistant cannot expose passwords, JWTs, KYC document numbers, database information, or application secrets
The assistant is instructed not to fabricate account or transaction information
Conversation Memory

The AI assistant uses Spring AI's chat-memory infrastructure with JDBC persistence.

Conversation IDs are scoped using the authenticated customer ID:

customer-{customerId}-conversation-{conversationId}

This prevents two customers from using the same client-provided conversation ID to access the same conversation-memory namespace.

Conversation memory is persisted so that context can be maintained across multiple assistant requests.

AI API
POST /api/assistant/chat

Access:

CUSTOMER

Example request:

{
"message": "What is my current account balance?",
"conversationId": "my-conversation"
}

Example response structure:

{
"conversationId": "my-conversation",
"response": "..."
}

A conversation ID is optional. If one is not provided, the backend generates one.

KYC Management

KYC functionality is included as an existing backend module.

Customer KYC
Customer KYC document submission
Document type and document number storage
Customer ownership verification
KYC lifecycle tracking
PENDING
UNDER_REVIEW
APPROVED
REJECTED
Duplicate submission prevention while under review
Prevention of modification after approval
Rejection reason tracking
Submission timestamp
Review timestamp
Administrative KYC
Admin-only pending KYC retrieval
KYC approval
KYC rejection
Rejection reason tracking
Paginated pending-KYC retrieval
KYC API
Method	Endpoint	Access
POST	/api/customers/{customerId}/kyc	CUSTOMER
GET	/api/customers/{customerId}/kyc	CUSTOMER
GET	/api/admin/kyc	ADMIN
PUT	/api/admin/kyc/{customerId}/approve	ADMIN
PUT	/api/admin/kyc/{customerId}/reject	ADMIN
API Overview
Authentication
Method	Endpoint	Access
POST	/api/auth/login	Public
Customers
Method	Endpoint	Access
POST	/api/customers	Public
GET	/api/customers/me	CUSTOMER
GET	/api/customers/{id}	CUSTOMER / ADMIN
GET	/api/customers	ADMIN
PUT	/api/customers/{id}	CUSTOMER / ADMIN
Accounts
Method	Endpoint	Access
POST	/api/customers/{customerId}/accounts	CUSTOMER
GET	/api/customers/{customerId}/accounts	CUSTOMER
GET	/api/accounts/{accountNumber}	CUSTOMER
GET	/api/accounts/my	CUSTOMER
GET	/api/accounts	ADMIN
Transactions
Method	Endpoint	Access
POST	/api/accounts/{accountNumber}/deposit	CUSTOMER
POST	/api/accounts/{accountNumber}/withdraw	CUSTOMER
POST	/api/accounts/{sourceAccountNumber}/transfer	CUSTOMER
GET	/api/accounts/{accountNumber}/transactions	CUSTOMER
GET	/api/accounts/transactions/{reference}	CUSTOMER
Transaction History Filters

The transaction history endpoint supports optional query parameters:

type
fromDate
toDate
minAmount
maxAmount
search
page
size
sort

Example:

GET /api/accounts/FIN10000001/transactions?type=TRANSFER&minAmount=1000&maxAmount=10000&search=rent&page=0&size=10
AI Assistant
Method	Endpoint	Access
POST	/api/assistant/chat	CUSTOMER
API Security Model

The application uses Spring Security with stateless JWT authentication.

HTTP Request
│
▼
JWT Authentication Filter
│
▼
JWT Validation
│
▼
SecurityContext
│
▼
Role / Ownership Authorization
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
MySQL

For customer-owned resources, authorization is enforced at multiple levels.

For example:

Authenticated Customer
│
▼
Current Customer ID
│
▼
Account / Transaction Ownership Check
│
▼
Business Operation

This prevents users from accessing another customer's financial information simply by changing an ID or account number in a request.

API & Error Handling

The application follows RESTful API principles and uses DTO-based request and response models.

Validation

Request DTOs use Jakarta Bean Validation for:

Required fields
Email validation
Password length
Account information
Transaction amounts
Transfer destinations
KYC fields
AI assistant messages
Request field lengths
Centralized Exception Handling

The application provides centralized exception handling for application-level errors including:

Invalid credentials
Customer not found
Account not found
Account not active
Insufficient balance
Same-account transfer
Transaction not found
Duplicate email
Invalid transaction filters
Validation failures
Authentication failures
Authorization failures

Responses are returned using structured error objects rather than exposing internal implementation details.

Technology Stack
Technology	Purpose
Java 17+	Programming language
Spring Boot 4.1.0	Backend framework
Spring Security	Authentication & authorization
Spring Data JPA	Data persistence
Hibernate	ORM
MySQL	Relational database
JWT / JJWT 0.13.0	Stateless authentication
Spring AI 2.0.1	AI integration
Google Gemini	AI model
Spring AI JDBC Chat Memory	Persistent conversation memory
Jakarta Bean Validation	Request validation
Maven	Dependency management
JUnit 5	Automated testing
Mockito	Unit testing
Spring Security Test	Security testing
Spring Boot Test	Integration testing
Postman / IntelliJ HTTP Client	API testing
Architecture

FinBank follows a layered backend architecture.

                    Client
                      │
                      ▼
             ┌─────────────────┐
             │   Controllers   │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │    Services     │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │   Repositories  │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │      MySQL      │
             └─────────────────┘

Security is applied before protected controllers:

HTTP Request
│
▼
JWT Authentication Filter
│
▼
Spring Security
│
├── Authentication
│
└── Authorization
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
MySQL

The AI assistant follows a separate application flow:

Customer
│
▼
POST /api/assistant/chat
│
▼
FinancialAssistantController
│
▼
FinancialAssistantService
│
├───────────────► Customer-scoped Chat Memory
│
▼
Spring AI ChatClient
│
▼
Google Gemini
│
├── getMyAccounts()
├── getMyRecentTransactions()
├── getMyAccount()
└── getMyTransaction()
│
▼
Backend Database

The AI model does not receive unrestricted database access. Financial information is retrieved through controlled application tools.

Project Structure
src
├── main
│   ├── java/com/finbank
│   │   ├── ai
│   │   │   ├── FinancialAssistantConfig.java
│   │   │   └── FinancialAssistantTools.java
│   │   │
│   │   ├── config
│   │   │   └── SecurityConfig.java
│   │   │
│   │   ├── controller
│   │   │   ├── AccountController.java
│   │   │   ├── AdminKycController.java
│   │   │   ├── AuthController.java
│   │   │   ├── CustomerController.java
│   │   │   ├── FinancialAssistantController.java
│   │   │   └── TransactionController.java
│   │   │
│   │   ├── dto
│   │   │   ├── AccountRequestDto.java
│   │   │   ├── AccountResponseDto.java
│   │   │   ├── AssistantChatRequestDto.java
│   │   │   ├── AssistantChatResponseDto.java
│   │   │   ├── CustomerRequestDto.java
│   │   │   ├── CustomerResponseDto.java
│   │   │   ├── CustomerUpdateRequestDto.java
│   │   │   ├── DepositRequestDto.java
│   │   │   ├── KycRequestDto.java
│   │   │   ├── KycResponseDto.java
│   │   │   ├── LoginRequestDto.java
│   │   │   ├── LoginResponseDto.java
│   │   │   ├── TransactionFilterDto.java
│   │   │   ├── TransactionResponseDto.java
│   │   │   ├── TransferRequestDto.java
│   │   │   └── WithdrawalRequestDto.java
│   │   │
│   │   ├── entity
│   │   │   ├── Account.java
│   │   │   ├── Customer.java
│   │   │   ├── Transaction.java
│   │   │   ├── User.java
│   │   │   └── enums
│   │   │
│   │   ├── exception
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── application exceptions
│   │   │
│   │   ├── repository
│   │   │   ├── AccountRepository.java
│   │   │   ├── CustomerRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   ├── security
│   │   │   └── JwtAuthenticationFilter.java
│   │   │
│   │   └── service
│   │       ├── AccountService.java
│   │       ├── AuthService.java
│   │       ├── CurrentUserService.java
│   │       ├── CustomUserDetailsService.java
│   │       ├── CustomerService.java
│   │       ├── FinancialAssistantService.java
│   │       ├── JwtService.java
│   │       ├── KycService.java
│   │       └── TransactionService.java
│   │
│   └── resources
│       ├── application.properties
│       └── application-local.properties
│
└── test
└── java/com/finbank
├── ai
│   ├── FinancialAssistantConversationSecurityTest.java
│   ├── FinancialAssistantIntegrationTest.java
│   ├── FinancialAssistantMemoryIntegrationTest.java
│   ├── FinancialAssistantSecurityTest.java
│   ├── FinancialAssistantToolsTest.java
│   └── GeminiConnectionTest.java
│
├── controller
│   └── FinancialAssistantControllerIntegrationTest.java
│
└── service
├── AccountServiceTest.java
├── AuthServiceTest.java
├── CurrentUserServiceTest.java
├── CustomUserDetailsServiceTest.java
├── CustomerServiceTest.java
├── JwtServiceTest.java
└── TransactionServiceTest.java
Testing

The project contains an automated test suite covering both core banking functionality and the AI subsystem.

Service Tests

Unit tests cover:

Authentication
JWT generation and validation
Current-user resolution
User details loading
Customer management
Account management
Deposits
Withdrawals
Transfers
Balance validation
Account ownership
Transaction ownership
Transaction filtering validation
Security Tests

Security-focused tests cover:

Customer ownership enforcement
Account isolation
Transaction isolation
AI tool isolation
Authentication requirements
Role-based authorization
Customer-scoped conversation IDs
Prevention of cross-customer conversation access
AI Tests

The AI subsystem is tested for:

Account retrieval
Recent transaction retrieval
Individual account retrieval
Transaction lookup
Tool security
Tool parameter limits
Customer isolation
General financial questions
Conversation memory
Conversation ID handling
AI controller validation
Customer-only assistant access
Gemini connectivity
Integration Tests

Spring Boot integration tests verify the application context and AI/controller integration with the actual application configuration.

Running Locally
Prerequisites
Java 17 or higher
Maven
MySQL 8+
Google Gemini API key
IntelliJ IDEA or another Java IDE
Database

Create a MySQL database:

CREATE DATABASE finbank;
Environment Configuration

The application reads sensitive configuration from environment variables.

Required variables include:

DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
GOOGLE_API_KEY

Example:

DB_URL=jdbc:mysql://localhost:3306/finbank
DB_USERNAME=YOUR_USERNAME
DB_PASSWORD=YOUR_PASSWORD

JWT_SECRET=YOUR_JWT_SECRET

GOOGLE_API_KEY=YOUR_GOOGLE_GEMINI_API_KEY

The JWT secret must be sufficiently long for the configured signing algorithm.

Do not commit real database credentials, JWT secrets, or Google API keys to Git.

Local Profile

The application uses the local Spring profile during local development.

Environment-specific credentials should remain outside version control.

Run the Application

Using Maven:

./mvnw spring-boot:run

On Windows:

.\mvnw.cmd spring-boot:run

The backend runs on:

http://localhost:8080
Example Authentication Flow
1. Register a customer
   POST /api/customers
   Content-Type: application/json

Example:

{
"firstName": "Aniket",
"lastName": "Singh",
"email": "aniket@example.com",
"phone": "9876543210",
"dateOfBirth": "2000-01-01",
"password": "password123"
}
2. Login
   POST /api/auth/login
   Content-Type: application/json

Example:

{
"email": "aniket@example.com",
"password": "password123"
}

The response contains a JWT token.

3. Use the JWT

Protected endpoints require:

Authorization: Bearer <JWT_TOKEN>
Example Transaction Request
Deposit
POST /api/accounts/{accountNumber}/deposit
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Example:

{
"amount": 500.00,
"description": "Cash deposit"
}
Withdrawal
POST /api/accounts/{accountNumber}/withdraw
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Transfer
POST /api/accounts/{sourceAccountNumber}/transfer
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Example:

{
"destinationAccountNumber": "FIN10000002",
"amount": 1000.00,
"description": "Account transfer"
}
Example AI Assistant Request
POST /api/assistant/chat
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Example:

{
"message": "Show me my recent transactions",
"conversationId": "personal-finance"
}

The assistant retrieves the customer's financial information through the application's secured financial tools.

Security Considerations

FinBank implements several security mechanisms:

Stateless JWT authentication
BCrypt password hashing
Spring Security role-based authorization
Customer ownership checks
Account ownership checks
Transaction ownership checks
JWT validation before authentication
Centralized authentication error handling
Centralized authorization error handling
Input validation
Environment-based secret configuration
Transactional financial operations
Pessimistic account locking
Customer-scoped AI conversation memory
Read-only AI financial tools
Protection against exposing internal financial data through the AI assistant

Sensitive configuration files should never be committed to version control.

Design Principles

The project emphasizes:

Separation of concerns
Layered architecture
DTO-based API contracts
Repository abstraction
Service-level business logic
Secure authentication
Role-based authorization
Customer data isolation
Transactional financial operations
Database-level locking for account updates
Input validation at API boundaries
Centralized exception handling
Read-only AI tool access
Customer-scoped AI conversations
Maintainable and extensible code structure
Automated testing
Current Backend Status

The FinBank backend currently provides the core banking functionality required for the application:

Authentication
│
▼
Customer Management
│
▼
Account Management
│
▼
Financial Transactions
│
├── Deposit
├── Withdrawal
└── Transfer
│
▼
Transaction History & Filtering
│
▼
Secure REST APIs
│
▼
AI Financial Assistant
│
├── Gemini
├── Tool Calling
└── Persistent Conversation Memory

The backend is therefore ready to serve as the API layer for a frontend banking application.

Future Improvements

Potential future extensions include:

React-based banking dashboard
Customer-facing frontend
Admin dashboard
Account statements and downloadable reports
Advanced transaction analytics
Notification system
Email/SMS notifications
Refresh-token authentication
Audit logging
Docker containerization
CI/CD pipeline
Production database configuration
Cloud deployment
Monitoring and observability
Automated API documentation with OpenAPI/Swagger