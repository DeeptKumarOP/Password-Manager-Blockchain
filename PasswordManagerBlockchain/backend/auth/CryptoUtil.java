package backend.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CryptoUtil {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // in bits

    // 1. PBKDF2 - Derive a 256-bit Master Key from the Master Password
    public static byte[] deriveMasterKey(String password, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 600000, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    // 2. Derive Server Auth Hash (Server never sees Master Key, only this derived hash)
    public static String deriveAuthHash(byte[] masterKey, String authSalt) throws Exception {
        // Simple hash stretching of the master key so the server can verify it
        PBEKeySpec spec = new PBEKeySpec(Base64.getEncoder().encodeToString(masterKey).toCharArray(), authSalt.getBytes(), 10000, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return Base64.getEncoder().encodeToString(skf.generateSecret(spec).getEncoded());
    }

    // 3. AES-256-GCM Encryption
    public static String encryptAESGCM(byte[] key, String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) return "";
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

        // Combine IV and ciphertext for storage
        byte[] combined = new byte[GCM_IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertext, 0, combined, GCM_IV_LENGTH, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // 4. AES-256-GCM Decryption
    public static String decryptAESGCM(byte[] key, String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) return "";

        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

        byte[] ciphertext, ciphertextBuffer = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertextBuffer, 0, ciphertextBuffer.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] plaintext = cipher.doFinal(ciphertextBuffer);

        return new String(plaintext);
    }

    // 5. Generate ECDSA KeyPair for Identity Verification
    public static KeyPair generateECDSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        return keyGen.generateKeyPair();
    }

    // 6. Sign Data using ECDSA Private Key
    public static String signData(PrivateKey privateKey, String data) throws Exception {
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(data.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    // 7. Verify Data Signature using ECDSA Public Key
    public static boolean verifySignature(PublicKey publicKey, String data, String signatureStr) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes("UTF-8"));
            byte[] signature = Base64.getDecoder().decode(signatureStr);
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    // Helper: Keys to String
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // Helper: String to Keys
    public static PrivateKey stringToPrivateKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    public static PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }
}
