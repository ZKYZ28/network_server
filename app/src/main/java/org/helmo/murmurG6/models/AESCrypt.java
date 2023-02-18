package org.helmo.murmurG6.models;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class AESCrypt {

    private static final String AES_ALGORITHM = "AES";
    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Cette méthode prend un message en clair, une clé secrète et un vecteur d'initialisation (IV) en entrée.
     * Elle utilise la clé et l'IV pour chiffrer le message en utilisant l'algorithme de chiffrement AES-GCM.
     * Elle renvoie le texte chiffré résultant.
     * @param plaintext le message que l'on souhaite chiffrer
     * @param keyBase64AES clé sous la forme base64
     * @return le message chiffré sous forme d'une chaine de caractères
     */
    public static String encrypt(String plaintext, String keyBase64AES) throws Exception {
        // Conversion du message en tableau d'octets
        byte[] plaintextBytes = plaintext.getBytes();
        // Décoder la clé en base64 sous la forme d'un tableau d'octets
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64AES);

        // Génération d'un vecteur d'initialisation (IV) aléatoire
        byte[] IV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);

        // Création d'une clé secrète à partir de la clé fournie
        SecretKey key = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        // Chiffrement du message en utilisant l'algorithme AES/GCM
        byte[] cipherTextBytes = encrypt(plaintextBytes, key, IV);

        // Combine l'IV et le texte chiffré en un seul tableau d'octets
        ByteBuffer byteBuffer = ByteBuffer.allocate(IV.length + cipherTextBytes.length);
        byteBuffer.put(IV);
        byteBuffer.put(cipherTextBytes);
        byte[] cipherMessage = byteBuffer.array();

        // Encodage du tableau d'octets combiné en base64 et renvoi du résultat
        return Base64.getEncoder().encodeToString(cipherMessage);
    }

    /**
     * Cette méthode prend un texte chiffré, une clé secrète et un vecteur d'initialisation (IV) en entrée.
     * Elle utilise la clé et l'IV pour déchiffrer le texte en utilisant l'algorithme de chiffrement AES-GCM.
     * Elle renvoie le message en clair résultant.
     * @param cipherMessage le message chiffré (chaine de caractères)
     * @param keyBase64AES la clé sous la forme base64
     * @return le message déchiffré sous forme d'une chaine de caractères
     */
    public static String decrypt(String cipherMessage, String keyBase64AES) throws Exception
    {
        // Décodage du message chiffré en base64 sous la forme d'un tableau d'octets
        byte[] cipherMessageBytes = Base64.getDecoder().decode(cipherMessage);
        // Décodage de la clé en base64 sous la forme d'un tableau d'octets
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64AES);
        // Récupération de l'IV et du texte chiffré à partir du tableau d'octets
        byte[] IV = Arrays.copyOfRange(cipherMessageBytes, 0, GCM_IV_LENGTH);
        byte[] cipherTextBytes = Arrays.copyOfRange(cipherMessageBytes, GCM_IV_LENGTH, cipherMessageBytes.length);
        // Création d'une clé secrète à partir de la clé fournie
        SecretKey key = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        // Déchiffrement du message en utilisant l'algorithme AES/GCM
        byte[] decryptedTextBytes = decrypt(cipherTextBytes, key, IV);
        // Conversion du texte déchiffré en une chaîne de caractères et renvoi du résultat
        return new String(decryptedTextBytes);
    }

    /**
     * Effectue le chiffrement de données en utilisant l'algorithme AES avec le mode de chiffrement GCM.
     * @param plaintext les données à chiffrer.
     * @param key la clé de chiffrement.
     * @param IV le vecteur d'initialisation.
     * @return les données chiffrées.
     * @throws Exception si une erreur se produit pendant le chiffrement.
     */
    private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception
    {
        // Récupère une instance de Cipher
        Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);

        // Crée un SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_ALGORITHM);

        // Crée un GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        // Initialise le Cipher en mode ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        // Effectue le chiffrement
        return cipher.doFinal(plaintext);
    }

    /**
     * Effectue le déchiffrement de données en utilisant l'algorithme AES avec le mode de chiffrement GCM.
     * @param cipherText les données chiffrées à déchiffrer.
     * @param key la clé de déchiffrement.
     * @param IV le vecteur d'initialisation.
     * @return les données déchiffrées.
     * @throws Exception si une erreur se produit pendant le déchiffrement.
     */
    private static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] IV) throws Exception
    {
        // Récupère une instance de Cipher
        Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);

        // Crée un SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_ALGORITHM);

        // Crée un GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        // Initialise le Cipher en mode DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        // Effectue le déchiffrement
        return cipher.doFinal(cipherText);
    }
}

