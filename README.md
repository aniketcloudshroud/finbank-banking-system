# 🏦 FinBank

### Secure Banking Management System with an AI-Powered Financial Assistant

FinBank is a secure backend banking management system built with **Java and Spring Boot**. It provides customer authentication, account management, financial transaction processing, transaction history and filtering, role-based authorization, and an **AI-powered financial assistant** using Spring AI and Google Gemini.

The application follows a layered architecture with clear separation between controllers, services, repositories, entities, DTOs, security, exception handling, and AI components.

---

## 🚀 Highlights

| Feature | Status |
|---|:---:|
| 🔐 JWT Authentication | ✅ |
| 👥 Customer Management | ✅ |
| 🏦 Account Management | ✅ |
| 💰 Deposits & Withdrawals | ✅ |
| 🔄 Account Transfers | ✅ |
| 📊 Transaction History & Filtering | ✅ |
| 🛡️ Role-Based Authorization | ✅ |
| 🔒 Customer Data Isolation | ✅ |
| 🤖 AI Financial Assistant | ✅ |
| 🧠 Persistent AI Conversation Memory | ✅ |
| 🧪 Automated Test Suite | ✅ |
| 🌐 Frontend | 🚧 Next Phase |

---

# 📋 Table of Contents

- [Features](#-features)
- [AI Financial Assistant](#-ai-powered-financial-assistant)
- [Security Architecture](#-security-architecture)
- [API Overview](#-api-overview)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Example API Usage](#-example-api-usage)
- [Design Principles](#-design-principles)
- [Backend Status](#-backend-status)
- [Future Improvements](#-future-improvements)

---

# ✨ Features

## 🔐 Authentication & Authorization

- Customer registration using email and password
- Customer login
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based authorization
- `CUSTOMER` and `ADMIN` roles
- Protected customer and administrative endpoints
- Custom JWT authentication filter
- Centralized authentication and authorization error handling
- Customer ownership verification
- Prevention of cross-customer data access

---

## 👤 Customer Management

- Customer registration
- Automatic user creation during registration
- BCrypt password encryption
- Email normalization
- Duplicate email validation
- Customer profile retrieval
- Authenticated customer profile retrieval
- Customer profile updates
- Paginated customer listing
- Customer ownership validation

---

## 🏦 Account Management

- Create bank accounts for customers
- Unique FinBank account number generation
- `FIN`-prefixed account numbers
- Multiple account types
- Multiple currencies
- Account status management
- Customer-specific account retrieval
- Paginated account listing
- Account ownership enforcement
- Protection against creating accounts for another customer

---

# 💳 Financial Transactions

FinBank supports the core banking operations required to manage customer funds.

### 💵 Deposits

- Deposit money into an account
- Account ownership verification
- Active-account validation
- Balance updates
- Transaction creation
- Unique transaction references
- Transaction status tracking
- Transaction timestamps

### 💸 Withdrawals

- Withdraw money from an account
- Account ownership verification
- Active-account validation
- Insufficient balance validation
- Balance updates
- Transaction creation
- Unique transaction references

### 🔄 Transfers

- Transfer funds between FinBank accounts
- Source account ownership verification
- Destination account validation
- Active-account validation
- Insufficient balance validation
- Same-account transfer prevention
- Atomic balance updates
- Transaction creation
- Unique transaction references

Financial transaction operations use database transactions and pessimistic account locking to provide safer concurrent balance updates.

---

# 📊 Transaction History & Filtering

FinBank provides customer-specific transaction history with pagination, sorting, and filtering.

### Supported Filters

- Transaction type
- From date
- To date
- Minimum amount
- Maximum amount
- Description search
- Transaction reference search
- Pagination
- Sorting

### Validation

The API validates transaction filter ranges:

```text
fromDate <= toDate
minAmount <= maxAmount
minAmount >= 0
maxAmount >= 0

Customers can only retrieve transactions associated with accounts they own.

🤖 AI-Powered Financial Assistant

FinBank includes an AI-powered financial assistant built using:

Spring AI
Google Gemini
Spring AI Tool Calling
JDBC-backed Chat Memory

The assistant provides customers with a conversational interface for understanding their financial information.

Important: The AI assistant is intentionally read-only. It cannot perform deposits, withdrawals, transfers, or other financial mutations.

🧠 AI Capabilities

The assistant can:

Retrieve customer accounts
Retrieve account balances
Retrieve account information
Retrieve recent transactions
Retrieve individual transactions
Explain transaction activity
Answer general financial education questions
Perform calculations using retrieved financial data
Maintain conversational context across requests
🛠️ AI Tool Calling

The AI assistant retrieves application data through controlled backend tools rather than having direct database access.

Tool	Purpose
getMyAccounts	Retrieve authenticated customer's accounts
getMyRecentTransactions	Retrieve recent transactions
getMyAccount	Retrieve a specific owned account
getMyTransaction	Retrieve a specific owned transaction

The tools derive the customer identity from the authenticated security context.

The client cannot provide an arbitrary customer ID to access another customer's financial information.

🔒 AI Security

The AI subsystem implements multiple security controls:

Authentication required
Customer-only access
Customer identity derived from Spring Security
Account ownership verification
Transaction ownership verification
No unrestricted database access for the LLM
No client-supplied customer IDs
Recent transaction limit capped between 1 and 50
Customer-scoped conversation IDs
Cross-customer conversation isolation
Read-only financial tools
No password or JWT exposure
No KYC document number exposure
No database or application secret exposure
No fabricated account or transaction data
💬 Persistent Conversation Memory

The assistant uses JDBC-backed persistent chat memory.

Conversation IDs are scoped to the authenticated customer:

customer-{customerId}-conversation-{conversationId}

This prevents two different customers from sharing the same conversation-memory namespace.

Conversation context can therefore persist across multiple requests while remaining isolated between customers.

🌐 API Overview
🔑 Authentication
Method	Endpoint	Access
POST	/api/auth/login	Public
👤 Customers
Method	Endpoint	Access
POST	/api/customers	Public
GET	/api/customers/me	CUSTOMER
GET	/api/customers/{id}	CUSTOMER / ADMIN
GET	/api/customers	ADMIN
PUT	/api/customers/{id}	CUSTOMER / ADMIN
🏦 Accounts
Method	Endpoint	Access
POST	/api/customers/{customerId}/accounts	CUSTOMER
GET	/api/customers/{customerId}/accounts	CUSTOMER
GET	/api/accounts/{accountNumber}	CUSTOMER
GET	/api/accounts/my	CUSTOMER
GET	/api/accounts	ADMIN
💳 Transactions
Method	Endpoint	Access
POST	/api/accounts/{accountNumber}/deposit	CUSTOMER
POST	/api/accounts/{accountNumber}/withdraw	CUSTOMER
POST	/api/accounts/{sourceAccountNumber}/transfer	CUSTOMER
GET	/api/accounts/{accountNumber}/transactions	CUSTOMER
GET	/api/accounts/transactions/{reference}	CUSTOMER
Transaction History Query Parameters
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
🤖 AI Assistant
Method	Endpoint	Access
POST	/api/assistant/chat	CUSTOMER

Example request:

{
  "message": "Show me my recent transactions",
  "conversationId": "personal-finance"
}
🪪 KYC Management

KYC functionality is included as part of the backend.

Customer KYC
KYC document submission
Document type and document number
Customer ownership verification
KYC lifecycle tracking
PENDING
UNDER_REVIEW
APPROVED
REJECTED
Duplicate submission prevention
Modification prevention after approval
Rejection reason tracking
Submission timestamps
Review timestamps
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
🛡️ Security Architecture

FinBank uses stateless JWT authentication with Spring Security.

                    HTTP Request
                         │
                         ▼
              ┌─────────────────────┐
              │ JWT Authentication   │
              │      Filter          │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │  Spring Security    │
              │ Authentication &    │
              │   Authorization     │
              └──────────┬──────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ Controller  │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │   Service   │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ Repository  │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │    MySQL    │
                  └─────────────┘

Customer-owned resources follow an additional ownership check:

Authenticated User
       │
       ▼
Current Customer ID
       │
       ▼
Ownership Verification
       │
       ├── Account Ownership
       │
       └── Transaction Ownership
              │
              ▼
        Business Operation
🏗️ Project Architecture

The application follows a layered architecture:

┌─────────────────────────────────────────┐
│                Client                   │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│             Controller Layer            │
│        REST API / Request Handling      │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│              Service Layer              │
│       Business Logic & Validation       │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│            Repository Layer              │
│          Data Access / JPA              │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│                MySQL                    │
│              Persistence                │
└─────────────────────────────────────────┘

The AI subsystem operates alongside the standard REST flow:

Customer
   │
   ▼
/api/assistant/chat
   │
   ▼
FinancialAssistantController
   │
   ▼
FinancialAssistantService
   │
   ├───────────────► Persistent Chat Memory
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
      FinBank Services
            │
            ▼
          MySQL
📁 Project Structure
src/
│
├── main/
│   │
│   ├── java/com/finbank/
│   │   │
│   │   ├── ai/
│   │   │   ├── FinancialAssistantConfig.java
│   │   │   └── FinancialAssistantTools.java
│   │   │
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   │
│   │   ├── controller/
│   │   │   ├── AccountController.java
│   │   │   ├── AdminKycController.java
│   │   │   ├── AuthController.java
│   │   │   ├── CustomerController.java
│   │   │   ├── FinancialAssistantController.java
│   │   │   └── TransactionController.java
│   │   │
│   │   ├── dto/
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
│   │   ├── entity/
│   │   │   ├── Account.java
│   │   │   ├── Customer.java
│   │   │   ├── Transaction.java
│   │   │   ├── User.java
│   │   │   └── enums/
│   │   │
│   │   ├── exception/
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── Application Exceptions
│   │   │
│   │   ├── repository/
│   │   │   ├── AccountRepository.java
│   │   │   ├── CustomerRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   ├── security/
│   │   │   └── JwtAuthenticationFilter.java
│   │   │
│   │   └── service/
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
│   └── resources/
│       ├── application.properties
│       └── application-local.properties
│
└── test/
    │
    └── java/com/finbank/
        │
        ├── ai/
        │   ├── FinancialAssistantConversationSecurityTest.java
        │   ├── FinancialAssistantIntegrationTest.java
        │   ├── FinancialAssistantMemoryIntegrationTest.java
        │   ├── FinancialAssistantSecurityTest.java
        │   ├── FinancialAssistantToolsTest.java
        │   └── GeminiConnectionTest.java
        │
        ├── controller/
        │   └── FinancialAssistantControllerIntegrationTest.java
        │
        └── service/
            ├── AccountServiceTest.java
            ├── AuthServiceTest.java
            ├── CurrentUserServiceTest.java
            ├── CustomUserDetailsServiceTest.java
            ├── CustomerServiceTest.java
            ├── JwtServiceTest.java
            └── TransactionServiceTest.java
🧪 Testing

The project includes automated tests covering the core banking system, security layer, and AI subsystem.

Service Tests

Test coverage includes:

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

Security tests cover:

Authentication requirements
Role-based authorization
Customer ownership enforcement
Account isolation
Transaction isolation
AI tool isolation
Customer-scoped conversations
Cross-customer conversation protection
AI Tests

AI-related tests cover:

Account retrieval
Recent transaction retrieval
Individual account retrieval
Transaction lookup
Tool parameter limits
Tool security
Customer isolation
General financial questions
Conversation memory
Conversation ID handling
AI controller validation
Customer-only assistant access
Gemini connectivity
Integration Tests

Spring Boot integration tests verify:

Application context loading
AI integration
Controller integration
Security integration
Persistent conversation memory

All current automated tests pass successfully.

⚙️ Getting Started
Prerequisites

Make sure the following are installed:

Java 17+
Maven
MySQL 8+
Google Gemini API Key
IntelliJ IDEA or another Java IDE
🗄️ Database Setup

Create the FinBank database:

CREATE DATABASE finbank;
🔧 Configuration

The application uses environment-specific configuration.

Required environment variables:

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
⚠️ Never commit secrets

Do not commit:

application-local.properties

or any file containing:

Database passwords
JWT secrets
Gemini API keys
Other credentials
▶️ Running the Application
Linux / macOS
./mvnw spring-boot:run
Windows
.\mvnw.cmd spring-boot:run

The application starts on:

http://localhost:8080
🔑 Example Authentication Flow
1. Register
POST /api/customers
Content-Type: application/json
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
{
  "email": "aniket@example.com",
  "password": "password123"
}

The response contains a JWT token.

3. Access Protected APIs

Include the token in every protected request:

Authorization: Bearer <JWT_TOKEN>
💰 Example Transaction
Deposit
POST /api/accounts/{accountNumber}/deposit
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
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
{
  "destinationAccountNumber": "FIN10000002",
  "amount": 1000.00,
  "description": "Account transfer"
}
🤖 Example AI Request
POST /api/assistant/chat
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
{
  "message": "Show me my recent transactions",
  "conversationId": "personal-finance"
}

The assistant retrieves the customer's financial information through secured backend tools.

🧱 Design Principles

FinBank emphasizes:

Separation of concerns
Layered architecture
DTO-based API contracts
Repository abstraction
Service-level business logic
Secure authentication
Role-based authorization
Customer data isolation
Transactional financial operations
Pessimistic locking
Input validation
Centralized exception handling
Read-only AI tools
Customer-scoped AI memory
Automated testing
Maintainable and extensible code
