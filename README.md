# Decentralized Zero-Knowledge Password Manager (Java)

A security-focused password manager built in Java that combines Zero-Knowledge Cryptography, AES-256 encryption, and a blockchain-based tamper detection system.

This project demonstrates both secure system design and the application of core Object-Oriented Programming (OOP) principles in a real-world architecture.

---

## Overview

The system is designed to ensure that sensitive user data, including the master password, is never exposed to the database or server. All encryption and key derivation occur locally, and only encrypted data is stored.

A blockchain-inspired structure is used to maintain an immutable and verifiable record of all user actions and stored data. Any unauthorized modification to stored data is detected through cryptographic validation.

---

## Key Features

### Zero-Knowledge Authentication

* The system never stores or processes plaintext passwords
* A master key is derived using PBKDF2 with HmacSHA256
* Authentication is performed using a derived hash

### AES-256-GCM Encryption

* Vault data is encrypted using AES-256-GCM
* Provides both confidentiality and integrity
* Each encryption uses a unique initialization vector (IV)

### ECDSA Digital Signatures

* Each user is assigned an elliptic curve key pair (secp256r1)
* Every blockchain block is digitally signed
* Ensures authenticity and prevents tampering

### Blockchain-Based Integrity

* Data is stored as a chain of blocks
* Each block contains a hash linked to the previous block
* Any modification breaks the chain and is detected

### Tamper Detection

* Built-in validation checks both:

  * Hash integrity
  * Digital signatures
* Includes a simulation feature to demonstrate breach detection

---

## System Architecture

The application follows a layered architecture:

UI Layer (Swing)
→ Service Layer (AuthService)
→ Cryptographic Layer (CryptoUtil, HashUtil)
→ Blockchain Layer (Block, Blockchain)
→ Data Access Layer (DAO)
→ SQLite Database

Each layer is responsible for a distinct concern, ensuring modularity and maintainability.

---

## Object-Oriented Design

This project incorporates all major OOP principles:

### Encapsulation

* Sensitive data such as the master key is stored in private fields
* Access is controlled through methods

### Abstraction

* Interfaces such as `BlockRepository` and `EncryptionStrategy` hide implementation details
* High-level modules depend on abstractions, not concrete classes

### Inheritance

* The `Block` class extends an abstract base class to model shared blockchain behavior

### Polymorphism

* Runtime polymorphism through interfaces:

  * `BlockRepository` allows interchangeable storage implementations
  * `EncryptionStrategy` allows interchangeable encryption algorithms
* Compile-time polymorphism through method overloading in utility classes

### Design Patterns Used

* Strategy Pattern (encryption handling)
* Repository Pattern (data access abstraction)

---

## Technologies Used

* Java (JDK 8 or higher)
* Swing (for UI)
* SQLite (for storage)
* JDBC (database connectivity)

---

## Project Structure

backend/

* auth/

  * AuthService.java
  * CryptoUtil.java
  * HashUtil.java
  * EncryptionStrategy.java
  * AESGCMStrategy.java

* blockchain/

  * AbstractBlock.java
  * Block.java
  * Blockchain.java
  * ChainValidator.java
  * DefaultValidator.java

* db/

  * DBConnection.java
  * UserDAO.java
  * LogDAO.java
  * BlockRepository.java

* models/

  * User.java
  * LogEntry.java

frontend/

* LoginUI.java
* SignupUI.java
* DashboardUI.java

Main.java

---

## Setup and Execution

### Prerequisites

* Java JDK 8 or higher
* PowerShell (for running scripts)

### Steps

1. Navigate to the project directory:

   ```
   cd path/to/project
   ```

2. Build the project:

   ```
   powershell -ExecutionPolicy Bypass -File .\build.ps1
   ```

3. Run the application:

   ```
   powershell -ExecutionPolicy Bypass -File .\run.ps1
   ```

---

## How to Test Tamper Detection

1. Create a new user account
2. Log in to the system
3. Add one or more service-password entries
4. Use the "Simulate Breach" option in the dashboard
5. Run blockchain validation

The system will detect inconsistencies in hash values or signatures and report a breach.

---

## Learning Outcomes

This project demonstrates:

* Secure password management using modern cryptography
* Practical implementation of Zero-Knowledge systems
* Use of blockchain principles for data integrity
* Clean separation of concerns using OOP
* Application of design patterns in real-world systems

---

## Future Improvements

* Replace Swing UI with a web-based interface
* Introduce distributed blockchain nodes
* Add multi-device synchronization
* Enhance key management with hardware-backed security
* Add REST API layer for external integrations
