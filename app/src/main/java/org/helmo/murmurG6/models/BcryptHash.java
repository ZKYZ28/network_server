package org.helmo.murmurG6.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * @since 11 février 2023
 * @version 1.0
 */
public class BcryptHash {

    private final int rounds;
    private final String salt;
    private final String hash;

    public BcryptHash(int rounds, String salt, String hash) {
        this.rounds = rounds;
        this.salt = salt;
        this.hash = hash;
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


    /**
     * Décompose un hachage Bcrypt en ses composants.
     *
     * @param hashed le hachage Bcrypt à décomposer, qui doit avoir le format suivant : $2a$RR$SALT$HASH
     * où RR représente le nombre de tours de l'algorithme de hachage, SALT est la valeur de sel utilisée
     * lors du hachage et HASH est le hachage résultant.
     *
     * @return un objet BcryptHash contenant les informations décomposées : le nombre de tours (rounds), la valeur de sel (salt)
     * et le hachage (hash).
     */
    public static BcryptHash decomposeHash(String hashed) {
        int rounds = Integer.parseInt(hashed.substring(4, 6));
        String salt = hashed.substring(7, 29);
        String hash = hashed.substring(29);
        return new BcryptHash(rounds, salt, hash);
    }

    /**
     * La méthode calculateChallenge calcule le hachage de la chaîne de caractères aléatoires et du hachage BCrypt en utilisant l'algorithme SHA3-256.
     * Le hachage BCrypt inclut le nombre de tours et le sel associé au mot de passe.
     * Le résultat de la fonction de hachage est retourné sous forme hexadécimale.
     *
     * @param random La chaîne de caractères aléatoires reçue lors de la connexion avec le serveur.
     * @return Le hachage calculé sous forme hexadécimale.
     */
    public String calculateChallenge(String random) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-256");
            byte[] unHashedChallengeBytes = (random + this.toString()).getBytes();
            byte[] hashedBytes = md.digest(unHashedChallengeBytes);
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Cette méthode permet de convertir un tableau d'octets en une chaîne de caractères hexadécimale.
     *
     * @param bytes Le tableau d'octets à convertir.
     * @return La chaîne de caractères hexadécimale représentant le tableau d'octets.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "$2b$"+ this.rounds + "$" + this.salt + this.hash;
    }
}