<div align="center">

🏦 FinBank

Digital Banking • Secure APIs • AI-Powered Financial Assistant

A full-stack digital banking platform built with Spring Boot, React, TypeScript, MySQL, JWT Security, and Spring AI + Google Gemini.

<p>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React"/>
  <img src="https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript"/>
  <img src="https://img.shields.io/badge/MySQL-8+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Spring%20AI-2.0.1-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring AI"/>
  <img src="https://img.shields.io/badge/Gemini-AI-4285F4?style=flat-square&logo=google&logoColor=white" alt="Gemini"/>
  <img src="https://img.shields.io/badge/Vite-8-646CFF?style=flat-square&logo=vite&logoColor=white" alt="Vite"/>
</p>

</div>

✨ Overview

FinBank is a full-stack digital banking application designed to demonstrate how a modern banking platform can be built with a secure Java backend and a responsive React frontend.

It goes beyond basic CRUD operations by implementing:

🔐 JWT-based authentication and role-based authorization

👤 Customer and profile management

💳 Multiple banking accounts

💰 Deposits and withdrawals

🔄 Account-to-account transfers

📊 Transaction history with filtering and pagination

🪪 KYC workflow and status management

🤖 AI-powered financial assistant

🧠 Persistent AI conversation memory

🛡️ Customer-level data isolation

🧪 Unit, security, controller, AI, and integration testing

Project status: ✅ Core customer banking and AI assistant functionality is working.

🎯 What FinBank Demonstrates

This project was built to showcase practical full-stack engineering rather than simply connecting a frontend to a database.

Area

Implementation

Backend

Spring Boot REST APIs

Frontend

React + TypeScript + Vite

Database

MySQL

Authentication

JWT

Authorization

Spring Security + roles

ORM

Spring Data JPA / Hibernate

Validation

Jakarta Bean Validation

AI

Spring AI + Google Gemini

AI Memory

JDBC-backed Spring AI chat memory

API Client

Axios

Routing

React Router

Testing

JUnit + Mockito + Spring Boot tests

Build

Maven + npm

🚀 Features

🔐 Authentication & Security

FinBank implements a stateless authentication architecture.

Customer authentication

Customer registration

Customer login

JWT token generation

JWT validation

BCrypt password hashing

Protected API endpoints

Automatic JWT attachment from the frontend

Authorization

The application uses role-based access control:

CUSTOMER
ADMIN

Protected service methods can enforce roles using Spring Security.

Example:

@PreAuthorize("hasRole('CUSTOMER')")

Ownership protection

Authentication alone is not treated as sufficient.

For customer-owned resources, FinBank verifies:

JWT
 ↓
Authenticated User
 ↓
Current Customer
 ↓
Requested Resource
 ↓
Ownership Check
 ↓
Operation

This prevents a customer from changing an ID or account number in a request to access another customer's financial data.

💳 Banking Operations

Accounts

Customers can:

View their accounts

Open additional accounts

Select account type

View account numbers

View balances

View account status

View account creation information

Supported account types:

SAVINGS
CURRENT

Supported currency:

INR

💰 Deposits

A deposit:

Validates the authenticated customer

Validates account ownership

Checks account status

Validates the amount

Updates the account balance

Creates a transaction

Generates a unique transaction reference

💸 Withdrawals

A withdrawal:

Validates account ownership

Checks account status

Validates the requested amount

Checks sufficient balance

Updates the account balance

Creates a transaction

Generates a unique transaction reference

🔄 Transfers

FinBank supports transfers between FinBank accounts.

The backend validates:

Source-account ownership

Destination account existence

Source account status

Destination account status

Sufficient balance

Same-account transfer prevention

A successful transfer updates both balances and records the transaction.

Account balance updates are performed inside transactional service operations with pessimistic locking to provide safer concurrent balance updates.

📊 Transaction History

Customers can view transaction activity for their accounts.

Supported filters

Transaction type

Date range

Minimum amount

Maximum amount

Search text

Pagination

Sorting

Search supports:

Transaction reference
Transaction description

The backend also validates filter combinations such as:

fromDate <= toDate
minAmount <= maxAmount
amount >= 0

🪪 KYC

FinBank includes a backend KYC workflow.

Customer capabilities

Submit KYC information

Specify document type

Specify document number

View KYC status

View submission/review information

KYC states

PENDING
UNDER_REVIEW
APPROVED
REJECTED

Administrative capabilities

Administrators can:

Retrieve pending KYC submissions

Approve KYC

Reject KYC

Store rejection reasons

Paginate KYC results

🤖 FinBank AI

One of the key features of the project is its AI-powered financial assistant.

The assistant is built using:

React
   ↓
Spring Boot REST API
   ↓
FinancialAssistantService
   ↓
Spring AI ChatClient
   ↓
Google Gemini

The AI assistant can answer both general financial questions and customer-specific questions using controlled application tools.

🧰 AI Tool Calling

The model does not receive unrestricted access to the database.

Instead, Spring AI exposes controlled tools such as:

Tool

Purpose

getMyAccounts

Retrieve the authenticated customer's accounts

getMyRecentTransactions

Retrieve recent transactions

getMyAccount

Retrieve a specific owned account

getMyTransaction

Retrieve a specific owned transaction

The tools resolve the authenticated customer from the Spring Security context.

The user does not supply a customer ID to the tools.

Example

User
 │
 │ "Show me my recent transactions"
 ▼
Gemini
 │
 │ Tool call
 ▼
getMyRecentTransactions()
 │
 │ Current authenticated customer
 ▼
TransactionService
 │
 ▼
MySQL
 │
 ▼
Customer-owned transaction data
 │
 ▼
Gemini
 │
 ▼
Natural-language response

This design keeps database access inside the application's security boundary.

🧠 Persistent AI Conversation Memory

FinBank uses Spring AI JDBC chat memory.

Conversation history is stored in MySQL.

The application also scopes the conversation ID to the authenticated customer.

Conceptually:

Client Conversation ID
        +
Authenticated Customer ID
        ↓
Customer-scoped Conversation ID
        ↓
Spring AI JDBC Chat Memory
        ↓
MySQL

This prevents two customers from intentionally selecting the same client-side conversation ID and sharing chat memory.

🛡️ AI Security Model

The AI assistant is intentionally read-only.

It can retrieve and explain financial information, but it cannot perform banking mutations.

The assistant cannot:

❌ Deposit money

❌ Withdraw money

❌ Transfer money

❌ Change account balances

❌ Modify transactions

❌ Access another customer's accounts

❌ Access passwords

❌ Access JWT secrets

❌ Access database credentials

❌ Execute arbitrary SQL

It can:

✅ Read the customer's accounts

✅ Read the customer's transactions

✅ Explain banking concepts

✅ Answer financial education questions

✅ Perform supported calculations

✅ Maintain conversation context

🏗️ Architecture

High-level architecture

                    ┌─────────────────────┐
                    │    React Frontend   │
                    │  TypeScript + Vite  │
                    └──────────┬──────────┘
                               │
                         REST / JSON
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Spring Boot API   │
                    │     Controllers     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │      Services       │
                    │ Business Logic +    │
                    │ Security Checks     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Repositories / JPA  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       MySQL         │
                    │ Users / Customers   │
                    │ Accounts / Txns     │
                    │ KYC / AI Memory     │
                    └─────────────────────┘

🔒 Security architecture

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
        Ownership Check
              │
              ▼
          Repository

🗂️ Project Structure

finbank/
│
├── src/
│   ├── main/
│   │   ├── java/com/finbank/
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
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   │
│   ├── resources/
│   │   ├── application.properties
│   │   └── application-local.properties
│   │
│   └── test/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── types/
│   │   ├── App.tsx
│   │   ├── App.css
│   │   └── index.css
│   │
│   ├── public/
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.ts
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md

🖥️ Frontend

The frontend is a React single-page application.

Main routes

Route

Description

/login

Customer login

/register

Customer registration

/dashboard

Banking overview

/accounts

Account management

/transactions

Transaction history

/money

Deposit / withdrawal

/transfer

Account transfers

/profile

Customer profile

/assistant

AI financial assistant

The application uses:

React 19

TypeScript

Vite

Axios

React Router

Lucide React

The frontend communicates with the backend through /api routes.

During development, Vite proxies those requests to the Spring Boot server.

🔌 REST API

All endpoints are prefixed with:

/api

Authentication

Method

Endpoint

Access

POST

/api/auth/login

Public

Customers

Method

Endpoint

Access

POST

/api/customers

Public

GET

/api/customers/me

CUSTOMER

GET

/api/customers/{id}

CUSTOMER / ADMIN

GET

/api/customers

ADMIN

PUT

/api/customers/{id}

CUSTOMER / ADMIN

Accounts

Method

Endpoint

Access

POST

/api/customers/{customerId}/accounts

CUSTOMER

GET

/api/customers/{customerId}/accounts

CUSTOMER

GET

/api/accounts/my

CUSTOMER

GET

/api/accounts/{accountNumber}

CUSTOMER

GET

/api/accounts

ADMIN

Transactions

Method

Endpoint

Access

POST

/api/accounts/{accountNumber}/deposit

CUSTOMER

POST

/api/accounts/{accountNumber}/withdraw

CUSTOMER

POST

/api/accounts/{sourceAccountNumber}/transfer

CUSTOMER

GET

/api/accounts/{accountNumber}/transactions

CUSTOMER

GET

/api/accounts/transactions/{reference}

CUSTOMER

KYC

Method

Endpoint

Access

POST

/api/customers/{customerId}/kyc

CUSTOMER

GET

/api/customers/{customerId}/kyc

CUSTOMER

GET

/api/admin/kyc

ADMIN

PUT

/api/admin/kyc/{customerId}/approve

ADMIN

PUT

/api/admin/kyc/{customerId}/reject

ADMIN

AI Assistant

Method

Endpoint

Access

POST

/api/assistant/chat

CUSTOMER

Example:

{
  "message": "What are my recent transactions?",
  "conversationId": "personal-finance"
}

🗄️ Database

FinBank uses MySQL as its relational database.

The database stores:

┌─────────────────────┐
│ users               │
├─────────────────────┤
│ customers           │
├─────────────────────┤
│ account             │
├─────────────────────┤
│ transaction         │
├─────────────────────┤
│ spring_ai_chat_memory│
└─────────────────────┘

The exact schema is generated/managed by the application's JPA and Spring AI configuration for the current development setup.

Create the database with:

CREATE DATABASE finbank;

⚙️ Local Setup

Prerequisites

Make sure the following are installed:

Java 21

Node.js + npm

MySQL 8+

Maven (optional — Maven Wrapper is included)

Google Gemini API key

Git

1. Clone the repository

git clone <YOUR_GITHUB_REPOSITORY_URL>
cd finbank

2. Create the MySQL database

CREATE DATABASE finbank;

3. Configure local properties

Create:

src/main/resources/application-local.properties

Configure your local environment values:

DB_URL=jdbc:mysql://localhost:3306/finbank
DB_USERNAME=YOUR_USERNAME
DB_PASSWORD=YOUR_PASSWORD

JWT_SECRET=YOUR_LONG_RANDOM_SECRET

GOOGLE_API_KEY=YOUR_GEMINI_API_KEY

Never commit application-local.properties to GitHub.

▶️ Running the Backend

From the project root:

Windows

.\mvnw.cmd spring-boot:run

Linux / macOS

./mvnw spring-boot:run

Backend:

http://localhost:8080

▶️ Running the Frontend

Open another terminal:

cd frontend
npm install
npm run dev

Frontend:

http://localhost:5173

🔑 Authentication Flow

┌──────────┐
│ Register │
└────┬─────┘
     │
     ▼
POST /api/customers
     │
     ▼
User + Customer
     │
     ▼
┌─────────┐
│  Login  │
└────┬────┘
     │
     ▼
POST /api/auth/login
     │
     ▼
JWT Token
     │
     ▼
Frontend stores token
     │
     ▼
Authorization: Bearer <JWT>
     │
     ▼
Protected APIs

🧪 Testing

Run the complete test suite with:

Windows

.\mvnw.cmd test

Linux / macOS

./mvnw test

The project includes testing around:

Service layer

Authentication

JWT processing

Current-user resolution

Customer management

Account management

Deposits

Withdrawals

Transfers

Balance validation

Ownership validation

Transaction filtering

Security

Authentication requirements

Role authorization

Customer ownership

Account isolation

Transaction isolation

AI tool isolation

Customer-scoped conversation IDs

AI

Financial tools

Tool security

Customer isolation

Conversation memory

Gemini connectivity

AI controller validation

General financial questions

Integration

Spring application context

Controller integration

AI integration

🧯 Error Handling

FinBank uses centralized exception handling through:

GlobalExceptionHandler

The application provides structured handling for common failures including:

Invalid credentials

Customer not found

Account not found

Inactive account

Insufficient balance

Same-account transfer

Transaction not found

Duplicate email

Invalid transaction filters

Validation errors

Authentication failures

Authorization failures

AI/API failures

Request validation is handled using Jakarta Bean Validation.

🔐 Security Considerations

The project intentionally keeps secrets outside source control.

Never commit:

Database passwords
JWT secrets
Gemini API keys
Production credentials
Private connection strings
.env files containing secrets

Before publishing to GitHub, verify:

git status

and:

git ls-files src/main/resources/application-local.properties

The second command should return nothing.

If credentials were previously exposed

Rotate:

MySQL password

JWT secret

Gemini API key

before making the repository public.

🐳 Deployment Roadmap

The application is structured for containerization.

A production deployment can be organized as:

                    Internet
                       │
                       ▼
                ┌──────────────┐
                │   Frontend   │
                │ React / Vite │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │ Spring Boot  │
                │ REST API     │
                └──────┬───────┘
                       │
              ┌────────┴─────────┐
              │                  │
              ▼                  ▼
        ┌───────────┐      ┌─────────────┐
        │   MySQL   │      │ Gemini API  │
        └───────────┘      └─────────────┘

For deployment, environment variables should be supplied by the hosting platform rather than committed to the repository.

The current Vite proxy:

/api → http://localhost:8080

is intended for local development and should be replaced with the appropriate production architecture when deploying.

📈 Future Improvements

Potential next steps include:

Dockerfile for backend

Dockerfile for frontend

Docker Compose for local full-stack setup

Free cloud deployment

Production MySQL

Flyway/Liquibase migrations

CI/CD with GitHub Actions

OpenAPI / Swagger documentation

Dedicated admin dashboard

Customer-facing KYC UI

PDF bank statements

Transaction analytics

Email/SMS notifications

Refresh-token authentication

Audit logging

API rate limiting

Production monitoring

Enhanced AI conversation history

🧭 Project Workflow

The complete customer journey can be summarized as:

             ┌──────────────┐
             │   Register   │
             └──────┬───────┘
                    ▼
             ┌──────────────┐
             │    Login     │
             └──────┬───────┘
                    ▼
             ┌──────────────┐
             │  Dashboard   │
             └──────┬───────┘
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
   Accounts     Transactions   Profile
       │            │
       ▼            ▼
    Deposit      Filters
    Withdraw
    Transfer
       │
       └────────────┐
                    ▼
             ┌──────────────┐
             │ FinBank AI   │
             └──────┬───────┘
                    ▼
             Google Gemini
                    │
                    ▼
             Customer-scoped
             financial data

📌 Current Status

Module

Status

Customer registration

✅

Login / JWT

✅

Protected routes

✅

Customer profile

✅

Account management

✅

Deposits

✅

Withdrawals

✅

Transfers

✅

Transaction history

✅

Transaction filtering

✅

KYC backend

✅

AI assistant

✅

Gemini integration

✅

AI tool calling

✅

Customer-scoped AI memory

✅

MySQL persistence

✅

Automated tests

✅

Docker deployment

🚧 Next step

👨‍💻 Development Philosophy

FinBank follows a few important design principles:

1. Security first

Customer identity comes from the authenticated security context rather than trusting client-provided IDs.

2. Business logic belongs in services

Controllers handle HTTP concerns while services handle banking rules and ownership validation.

3. Financial operations are transactional

Balance updates and transaction creation are treated as a single business operation.

4. AI is constrained

The AI assistant does not receive unrestricted database access.

5. The frontend is not trusted

All important validation and authorization rules are enforced on the backend.

6. Secrets stay outside source control

Local and production credentials belong in environment-specific configuration.

📜 License

No open-source license has currently been specified for this project.

If you plan to distribute or reuse FinBank as an open-source project, add an appropriate license such as MIT.

<div align="center">

🏦 FinBank

A secure full-stack banking application with an AI-powered financial assistant.

Built with ❤️ using Spring Boot + React + MySQL + Spring AI

</div>
