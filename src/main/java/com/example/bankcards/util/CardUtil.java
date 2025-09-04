package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CardUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    /**
     * Encrypts card number using AES algorithm
     *
     * @param data card number to encrypt
     * @return encrypted card number as Base64 string
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }
    /**
     * Decrypts card number using AES algorithm
     *
     * @param encryptedData encrypted card number as Base64 string
     * @return decrypted card number
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
    /**
     * Masks card number for display (shows only last 4 digits)
     *
     * @param number encrypted card number
     * @return masked card number string
     */
    public String mask(String number) {
        String decrypted = decrypt(number);
        if (decrypted.length() < 4) return decrypted;
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }
}