package backend.auth;

import backend.blockchain.Block;
import backend.blockchain.Blockchain;
import backend.db.LogDAO;
import backend.db.UserDAO;
import backend.models.LogEntry;
import java.security.KeyPair;
import java.security.PrivateKey;

public class AuthService {
    private Blockchain blockchain;
    private UserDAO userDAO;
    private LogDAO logDAO;
    
    private Block currentUserBlock = null;
    private byte[] currentMasterKey = null; // Zero-Knowledge session key

    public AuthService(Blockchain blockchain, UserDAO userDAO, LogDAO logDAO) {
        this.blockchain = blockchain;
        this.userDAO = userDAO;
        this.logDAO = logDAO;
    }
    
    public Block getCurrentUserBlock() { return currentUserBlock; }
    public byte[] getCurrentMasterKey() { return currentMasterKey; }

    public boolean signup(String username, String password) {
        if (!HashUtil.checkPasswordStrength(password)) {
            System.out.println("❌ Registration Failed: Password must be >= 8 chars, contain a number, an uppercase letter, and a special character.");
            return false;
        }

        if (blockchain.findUserBlock(username) != null) {
            System.out.println("❌ Username already exists!");
            return false;
        }

        try {
            String salt = HashUtil.generateSalt();
            String authSalt = HashUtil.generateSalt();
            
            // PBKDF2 Master Key & Auth Hash Derivation
            byte[] masterKey = CryptoUtil.deriveMasterKey(password, salt);
            String authHash = CryptoUtil.deriveAuthHash(masterKey, authSalt);
            
            // Generate ECDSA Identity
            KeyPair ecPair = CryptoUtil.generateECDSAKeyPair();
            String pubKeyStr = CryptoUtil.publicKeyToString(ecPair.getPublic());
            String privKeyStr = CryptoUtil.privateKeyToString(ecPair.getPrivate());
            
            // AES-256-GCM Encrypt Private Key so it's safely stored on blockchain
            String encryptedPrivKey = CryptoUtil.encryptAESGCM(masterKey, privKeyStr);

            Block newBlock = new Block(0, username, salt, authSalt, authHash, pubKeyStr, encryptedPrivKey, ""); 
            
            // Blockchain links and calculates hash via addBlock
            blockchain.addBlock(newBlock);
            
            // Sign the correctly calculated hash
            newBlock.signature = CryptoUtil.signData(ecPair.getPrivate(), newBlock.currentHash);
            
            userDAO.saveBlock(newBlock); 
            logDAO.saveLog(new LogEntry(username, "SIGNUP", System.currentTimeMillis()));
            return true;
        } catch (Exception e) {
            System.out.println("Crypto Error: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        Block userBlock = blockchain.findUserBlock(username);
        if (userBlock == null) return false;

        try {
            // Re-derive the Zero-Knowledge Auth Hash securely
            byte[] masterKey = CryptoUtil.deriveMasterKey(password, userBlock.salt);
            String computedAuthHash = CryptoUtil.deriveAuthHash(masterKey, userBlock.authSalt);
            
            if (computedAuthHash.equals(userBlock.passwordHash)) {
                logDAO.saveLog(new LogEntry(username, "LOGIN_SUCCESS", System.currentTimeMillis()));
                this.currentUserBlock = userBlock;
                this.currentMasterKey = masterKey; // Keep in memory for vault decryption!
                return true;
            } else {
                logDAO.saveLog(new LogEntry(username, "LOGIN_FAILED", System.currentTimeMillis()));
                return false;
            }
        } catch (Exception e) {
             return false;
        }
    }
    
    public void logout() {
        if (currentUserBlock != null) {
            logDAO.saveLog(new LogEntry(currentUserBlock.username, "LOGOUT", System.currentTimeMillis()));
            currentUserBlock = null;
            
            // Destroy memory references
            if (currentMasterKey != null) {
                java.util.Arrays.fill(currentMasterKey, (byte) 0);
                currentMasterKey = null;
            }
        }
    }

    public void addEncryptedVaultData(String encryptedServicePasswords) {
        if (currentUserBlock == null || currentMasterKey == null) return;
        
        try {
            // Reconstruct block identity from blockchain state
            Block updatedBlock = new Block(0, currentUserBlock.username, currentUserBlock.salt, currentUserBlock.authSalt, currentUserBlock.passwordHash, currentUserBlock.publicKey, currentUserBlock.encryptedPrivateKey, "");
            updatedBlock.servicePasswords = encryptedServicePasswords;
            
            blockchain.addBlock(updatedBlock);
            
            // Recover Private Key using MasterKey
            String rawPrivKeyStr = CryptoUtil.decryptAESGCM(currentMasterKey, currentUserBlock.encryptedPrivateKey);
            PrivateKey privKey = CryptoUtil.stringToPrivateKey(rawPrivKeyStr);
            
            // Sign the updated transaction block
            updatedBlock.signature = CryptoUtil.signData(privKey, updatedBlock.currentHash);
            
            userDAO.saveBlock(updatedBlock);
            this.currentUserBlock = updatedBlock; 
            
        } catch(Exception e) {
            System.out.println("❌ Failed to sign or encrypt vault: " + e.getMessage());
        }
    }
}
