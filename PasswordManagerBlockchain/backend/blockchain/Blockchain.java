package backend.blockchain;

import java.util.ArrayList;

public class Blockchain {
    public ArrayList<Block> chain;

    public Blockchain() {
        chain = new ArrayList<>();
    }

    public void addBlock(Block newBlock) {
        if (!chain.isEmpty()) {
            newBlock.previousHash = getLatestBlock().currentHash;
            newBlock.index = chain.size();
        } else {
            newBlock.previousHash = "0"; 
            newBlock.index = 0;
        }
        newBlock.currentHash = newBlock.calculateHash();
        chain.add(newBlock);
    }

    public Block getLatestBlock() {
        if (chain.isEmpty()) return null;
        return chain.get(chain.size() - 1);
    }
    
    public Block findUserBlock(String username) {
        for (int i = chain.size() - 1; i >= 0; i--) {
            Block b = chain.get(i);
            if (b.username.equals(username)) {
                return b;
            }
        }
        return null;
    }

    public boolean isChainValid() {
        if (chain.isEmpty()) return true;
        
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!currentBlock.currentHash.equals(currentBlock.calculateHash())) {
                System.out.println("❌ Hash Mismatch on Block " + currentBlock.index);
                return false;
            }
            if (!currentBlock.previousHash.equals(previousBlock.currentHash)) {
                System.out.println("❌ Chain Link Broken on Block " + currentBlock.index);
                return false;
            }
        }
        
        // Zero-Knowledge ECDSA Signature Verification
        for (Block b : chain) {
            String originalPubKeyStr = getOriginalPublicKey(b.username);
            if (originalPubKeyStr == null || originalPubKeyStr.isEmpty()) continue; // System block
            
            try {
                java.security.PublicKey pubKey = backend.auth.CryptoUtil.stringToPublicKey(originalPubKeyStr);
                if (!backend.auth.CryptoUtil.verifySignature(pubKey, b.currentHash, b.signature)) {
                    System.out.println("❌ ECDSA Signature Invalid on Block " + b.index);
                    return false;
                }
            } catch(Exception e) {
                System.out.println("❌ Critical ECDSA Error on Block " + b.index);
                return false;
            }
        }
        return true;
    }
    
    // Grabs user's identity from their registration block preventing spoofing
    private String getOriginalPublicKey(String username) {
        for (Block b : chain) {
            if (b.username.equals(username) && b.publicKey != null && !b.publicKey.isEmpty()) {
                return b.publicKey;
            }
        }
        return null;
    }
}
