package org.helmo.murmurG6.models;

import org.helmo.murmurG6.models.exceptions.AesException;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
     *
     * @param plaintext    le message que l'on souhaite chiffrer
     * @param keyBase64AES clé sous la forme base64
     * @return le message chiffré sous forme d'une chaine de caractères
     * @throws AesException
     */
    public static byte[] encrypt(String plaintext, String keyBase64AES) throws AesException {
        try {
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

            // Encodage du tableau d'octets combiné en base64 et renvoi du résultat
            return byteBuffer.array();
        } catch (IllegalArgumentException e) {
            throw new AesException("Erreur : La clé en base 64 n'est pas valide");
        } catch (AesException e) {
            throw new AesException("Erreur lors du chiffrement du message : " + e.getMessage());
        }
    }

    /**
     * Cette méthode prend un texte chiffré, une clé secrète et un vecteur d'initialisation (IV) en entrée.
     * Elle utilise la clé et l'IV pour déchiffrer le texte en utilisant l'algorithme de chiffrement AES-GCM.
     * Elle renvoie le message en clair résultant.
     *
     * @param cipherMessageBytes le message chiffré en octets (chaine de caractères)
     * @param keyBase64AES       la clé sous la forme base64
     * @return le message déchiffré sous forme d'une chaine de caractères
     * @throws AesException
     */
    public static String decrypt(byte[] cipherMessageBytes, String keyBase64AES) throws AesException {
        try {
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
        } catch (IllegalArgumentException e) {
            throw new AesException("Erreur : La clé en base64 n'est pas valide.");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AesException("Erreur : Le tableau d'octets chiffré est invalide.");
        } catch (AesException e) {
            throw new AesException("Erreur lors du déchiffrement du message : " + e.getMessage());
        }

    }

    /**
     * Effectue le chiffrement de données en utilisant l'algorithme AES avec le mode de chiffrement GCM.
     *
     * @param plaintext les données à chiffrer.
     * @param key       la clé de chiffrement.
     * @param IV        le vecteur d'initialisation.
     * @return les données chiffrées.
     * @throws AesException si une erreur se produit pendant le chiffrement.
     */
    private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws AesException {
        try {
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
        } catch (NoSuchAlgorithmException e) {
            throw new AesException("L'algorithme de chiffrement GCM n'est pas disponible.");
        } catch (NoSuchPaddingException e) {
            throw new AesException("Le mode de chiffrement GCM/PKCS5Padding n'est pas disponible.");
        } catch (InvalidKeyException e) {
            throw new AesException("La clé secrète n'est pas valide.");
        } catch (InvalidAlgorithmParameterException e) {
            throw new AesException("Les paramètres d'algorithme ne sont pas valides.");
        } catch (IllegalBlockSizeException e) {
            throw new AesException("La taille de bloc de chiffrement n'est pas valide.");
        } catch (BadPaddingException e) {
            throw new AesException("Le rembourrage de chiffrement est incorrect.");
        } catch (IllegalArgumentException e) {
            throw new AesException("L'IV n'est pas valide.");
        }
    }

    /**
     * Effectue le déchiffrement de données en utilisant l'algorithme AES avec le mode de chiffrement GCM.
     *
     * @param cipherText les données chiffrées à déchiffrer.
     * @param key        la clé de déchiffrement.
     * @param IV         le vecteur d'initialisation.
     * @return les données déchiffrées.
     * @throws AesException si une erreur se produit pendant le déchiffrement.
     */
    private static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] IV) throws AesException {
        try {
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
        } catch (NoSuchAlgorithmException e) {
            throw new AesException("L'algorithme de chiffrement GCM n'est pas disponible.");
        } catch (NoSuchPaddingException e) {
            throw new AesException("Le mode de chiffrement GCM/PKCS5Padding n'est pas disponible.");
        } catch (InvalidKeyException e) {
            throw new AesException("La clé secrète n'est pas valide.");
        } catch (InvalidAlgorithmParameterException e) {
            throw new AesException("Les paramètres d'algorithme ne sont pas valides.");
        } catch (IllegalBlockSizeException e) {
            throw new AesException("La taille de bloc de chiffrement n'est pas valide.");
        } catch (BadPaddingException e) {
            throw new AesException("Le rembourrage de chiffrement est incorrect.");
        } catch (IllegalArgumentException e) {
            throw new AesException("L'IV n'est pas valide.");
        }
    }

}

