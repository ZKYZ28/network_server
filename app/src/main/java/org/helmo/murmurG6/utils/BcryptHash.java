package org.helmo.murmurG6.utils;

public class BcryptHash {

    private int rounds;
    private String salt;
    private String hash;

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public static BcryptHash decomposeHash(String hash) {
        BcryptHash parts = new BcryptHash();
        parts.setRounds(Integer.parseInt(hash.substring(4, 6)));
        parts.setSalt(hash.substring(7, 29));
        parts.setHash(hash.substring(29));
        return parts;
    }

    public static void main(String[] args) {
        BcryptHash parts = BcryptHash.decomposeHash("$2b$14$azertyuiopmlkjhgfdsqwxMotDePasseHashé");
        System.out.println(parts.salt);
    }
}