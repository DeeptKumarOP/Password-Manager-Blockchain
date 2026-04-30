# Decentralized Zero-Knowledge Password Manager

A "Mega Startup Level", mathematically guaranteed Decentralized Password Manager built entirely in Java. Instead of a traditional central database, this system utilizes a cryptographic Blockchain ledger.

This application mimics the aggressive privacy architecture of services like **1Password** and **Bitwarden** natively within a CLI application by explicitly building a pristine Zero-Knowledge encryption engine.

## 🛡️ Top-Tier Features

* **Zero-Knowledge Architecture:** The application mathematically guarantees that the server and database *never* perceive or process any plaintext Master Passwords. The Master Key is derived and maintained entirely in local volatile session memory.
* **Military Grade Payload Encryption (`AES-256-GCM`):** All stored vaults and service passwords are encrypted locally into a secure AES-256 blob *before* ever touching the database backend.
* **PBKDF2 Key Derivation:** Uses rigorous hash stretching via `PBKDF2WithHmacSHA256` explicitly resisting brute force and dictionary hacking.
* **ECDSA Digital Signatures (`secp256r1`):** Every action requires the server to automatically verify Elliptical Curve cryptography signatures proving ownership on every block payload added. 
* **Immutable Blockchain State:** Logs and storage hashes are meticulously linked and can dynamically trace alterations. Includes embedded Tamper Detection algorithms explicitly validating both hash consistency and identity signatures.

## ⚙️ Prerequisites
* **Java SDK** (`JDK 8+` recommended)
* Works smoothly via provided compilation scripts (which handles fetching lightweight SQLite drivers cleanly).

## 🚀 Setup & Installation

Execute the provided automated compilation scripts using PowerShell to fetch the requisite data storage dependency (`sqlite-jdbc`) and boot up the system gracefully.

1. Open PowerShell and navigate to the project directory:
   ```powershell
   cd path/to/PasswordManagerBlockchain
   ```
   
2. Compile the project via the `build` script:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\build.ps1
   ```
   
3. Boot up the Dashboard Application:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\run.ps1
   ```

## 🔍 How to Test Tamper Detection
1. Run the application and choose option **1** to create an account securely.
2. Log in using option **2**.
3. Under the interactive dashboard, add some passwords via Option **1**.
4. Simulate an external database breach via Option **5** which deliberately artificially injects corrupt data and forged signatures.
5. Watch the Blockchain instantly detect mathematical discrepancies via Option **4**.
