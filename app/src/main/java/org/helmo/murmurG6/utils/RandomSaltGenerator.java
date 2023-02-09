package org.helmo.murmurG6.utils;

public class RandomSaltGenerator {

    private static final int SALT_SIZE = 22;
    private static final int[] NUMBERS = {0x30, 0x39};
    private static final int[] LETTERS_MIN = {0x61, 0x7A};
    private static final int[] LETTERS_MAJ = {0x41, 0x5A};
    private static final int[] SYMBOLS_ONE = {0x21, 0x2F};
    private static final int[] SYMBOLS_TWO = {0x3A, 0x40};
    private static final int[] SYMBOLS_THREE = {0x5B, 0x60};
    private static final int[][] ALL_CHARACTERS = { NUMBERS, LETTERS_MIN, LETTERS_MAJ, SYMBOLS_ONE, SYMBOLS_TWO, SYMBOLS_THREE };

    public static String generateSalt() {

        StringBuilder salt = new StringBuilder();

        for (int i = 0; i < SALT_SIZE; i++) {
            salt.append(generateRandomCharacter());
        }
        return salt.toString();
    }


    /**
     *
     * @return
     */
    private static char generateRandomCharacter() {

        //1. Choix du type de charactere aleratoirement
        int type =  (int)(Math.random() * ALL_CHARACTERS.length);

        //2. Choix du charactere dans le type choisi
        int range = (int)(Math.random() * (ALL_CHARACTERS[type][1] - ALL_CHARACTERS[type][0]));
        int selector = (int)(Math.random() * range);
        int character = ALL_CHARACTERS[type][0] + selector;

        return (char)character;
    }
}
