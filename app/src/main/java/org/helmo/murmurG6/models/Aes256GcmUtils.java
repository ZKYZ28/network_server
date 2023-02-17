package org.helmo.murmurG6.models;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class Aes256GcmUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private static final String base64AES = "P3FXqAUgfhy5cTjYdWlPQBJ/d6fdpbR88YsDPWPbo14\u003d";


    public static String encrypt(String plaintext, String keyBase64AES) throws Exception
    {
        byte[] plaintextBytes = plaintext.getBytes();
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64AES);
        byte[] IV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);

        SecretKey key = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        byte[] cipherTextBytes = encrypt(plaintextBytes, key, IV);

        // Combine IV and cipherText to a single byte array
        ByteBuffer byteBuffer = ByteBuffer.allocate(IV.length + cipherTextBytes.length);
        byteBuffer.put(IV);
        byteBuffer.put(cipherTextBytes);
        byte[] cipherMessage = byteBuffer.array();

        return Base64.getEncoder().encodeToString(cipherMessage);
    }

    public static String decrypt(String cipherMessage, String keyBase64AES) throws Exception
    {
        byte[] cipherMessageBytes = Base64.getDecoder().decode(cipherMessage);
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64AES);

        byte[] IV = Arrays.copyOfRange(cipherMessageBytes, 0, GCM_IV_LENGTH);
        byte[] cipherTextBytes = Arrays.copyOfRange(cipherMessageBytes, GCM_IV_LENGTH, cipherMessageBytes.length);

        SecretKey key = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        byte[] decryptedTextBytes = decrypt(cipherTextBytes, key, IV);

        return new String(decryptedTextBytes);
    }

    private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception
    {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_ALGORITHM);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Encryption

        return cipher.doFinal(plaintext);
    }

    private static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] IV) throws Exception
    {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_ALGORITHM);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Decryption
        return cipher.doFinal(cipherText);

    }

    public static void main(String[] args) throws Exception {
        String msg = "Coucou le pote";
        String msgEncrpt = encrypt(msg, base64AES);
        System.out.println("msgEcnrypt " + msgEncrpt);
        String msgDecrypt = decrypt(msgEncrpt, base64AES);
        System.out.println("msgDectypt " + msgDecrypt);
    }
}

