package org.helmo.murmurG6.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * <p>La classe BcryptHash représente un hachage Bcrypt d'un mot de passe.</p><br>
 *
 * <p>Elle contient les informations sur le nombre de tours utilisé pour le hachage (rounds),
 * la valeur de sel utilisée (salt), et le hachage résultant (hash).</p><br>
 *
 * <p>Cette classe fournit également des méthodes pour décomposer un hachage Bcrypt en ses composants,
 * calculer le hachage d'une chaîne de caractères aléatoire et du hachage BCrypt,
 * et convertir un tableau d'octets en une chaîne de caractères hexadécimale.</p>
 *
 * @version 1.0
 * @since 11 février 2023
 */
public class BCrypt {

    private final int rounds;
    private final String salt;
    private final String hash;

    public BCrypt(int rounds, String salt, String hash) {
        this.rounds = rounds;
        this.salt = salt;
        this.hash = hash;
    }

    public static BCrypt of(String hashed) {
        if (!Pattern.matches(Protocol.RX_BCRYPT_HASH, hashed)) {
            throw new IllegalArgumentException("Le hash bcrypt fourni n'est pas au format attendu. Veuillez vérifier que le hash est une chaîne valide conforme au format bcrypt.");
        }

        int rounds = Integer.parseInt(hashed.substring(4, 6));
        String salt = hashed.substring(7, 29);
        String hash = hashed.substring(29);

        return new BCrypt(rounds, salt, hash);
    }


    public String generateChallenge(String random) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Impossible de générer un challenge. L'algorithme de hachage SHA3-256 n'est pas disponible.", e);
        }

        byte[] unHashedChallengeBytes = (random + this).getBytes();
        byte[] hashedBytes = md.digest(unHashedChallengeBytes);

        return bytesToHex(hashedBytes);
    }


    /**
     * Cette méthode permet de convertir un tableau d'octets en une chaîne de caractères hexadécimale.
     *
     * @param bytes Le tableau d'octets à convertir.
     * @return La chaîne de caractères hexadécimale représentant le tableau d'octets.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public int getRounds() {
        return rounds;
    }

    public String getSalt() {
        return salt;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "$2b$" + this.rounds + "$" + this.salt + this.hash;
    }
}