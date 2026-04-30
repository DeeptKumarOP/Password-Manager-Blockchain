package backend.blockchain;

import backend.auth.HashUtil;

public class Block {
    public int index;
    public String username;
    public String salt; // Used for deriving Master Key
    public String authSalt; // Used for deriving Auth Hash from Master Key
    public String passwordHash; // This is now the PBKDF2 Auth Hash, the server never stores actual passwords
    
    public String publicKey; // User's ECDSA Identity
    public String encryptedPrivateKey; // AES-Encrypted Private Key
    public String signature; // ECDSA signature of the 'currentHash'
    
    public String previousHash;
    public String currentHash;
    public long timestamp;
    
    // Multiple accounts per user feature
    // Stored as an AES-256-GCM Encrypted Blob 
    public String servicePasswords = "";

    public Block(int index, String username, String salt, String authSalt, String passwordHash, String publicKey, String encryptedPrivateKey, String previousHash) {
        this.index = index;
        this.username = username;
        this.salt = salt;
        this.authSalt = authSalt;
        this.passwordHash = passwordHash;
        this.publicKey = publicKey == null ? "" : publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey == null ? "" : encryptedPrivateKey;
        this.previousHash = previousHash;
        this.timestamp = System.currentTimeMillis();
        this.currentHash = calculateHash();
    }
    
    // Used when loading from DB
    public Block(int index, String username, String salt, String authSalt, String passwordHash, String publicKey, String encryptedPrivateKey, String signature, String previousHash, String currentHash, long timestamp, String servicePasswords) {
        this.index = index;
        this.username = username;
        this.salt = salt;
        this.authSalt = authSalt;
        this.passwordHash = passwordHash;
        this.publicKey = publicKey == null ? "" : publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey == null ? "" : encryptedPrivateKey;
        this.signature = signature == null ? "" : signature;
        this.previousHash = previousHash;
        this.currentHash = currentHash;
        this.timestamp = timestamp;
        this.servicePasswords = servicePasswords == null ? "" : servicePasswords;
    }

    // Hash does NOT include signature, because signature signs the hash!
    public String calculateHash() {
        return HashUtil.calculateHash(index + username + passwordHash + publicKey + encryptedPrivateKey + previousHash + timestamp + salt + authSalt + servicePasswords);
    }
}
