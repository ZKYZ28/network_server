package org.helmo.murmurG6.utils;

/**
 * La classe RandomSaltGenerator génère un sel aléatoire de longueur spécifiée.
 */
public class RandomSaltGenerator {

    private static final int SALT_SIZE = 22; //La taille du sel généré.
    private static final int[] NUMBERS = {0x30, 0x39}; //L'ensemble des chiffres.
    private static final int[] LETTERS_MIN = {0x61, 0x7A}; //L'ensemble des lettres minuscules.
    private static final int[] LETTERS_MAJ = {0x41, 0x5A}; //L'ensemble des lettres majuscules.
    private static final int[] SYMBOLS_ONE = {0x21, 0x2F}; //Le premier ensemble de symboles.
    private static final int[] SYMBOLS_TWO = {0x3A, 0x40}; //Le deuxième ensemble de symboles.
    private static final int[] SYMBOLS_THREE = {0x5B, 0x60}; //Le troisième ensemble de symboles.
    private static final int[][] ALL_CHARACTERS = { NUMBERS, LETTERS_MIN, LETTERS_MAJ, SYMBOLS_ONE, SYMBOLS_TWO, SYMBOLS_THREE }; //L'ensemble de tous les types de caractères.

    /**
     * <p>La méthode generateSalt génère une chaîne aléatoire de caractères de longueur SALT_SIZE.</p>
     * <p>Pour générer la chaîne aléatoire, elle appelle la méthode <a href="#generateRandomCharacter()">generateRandomCharacter()</a> qui génère un caractère aléatoire à partir de différents types de caractères tels que les nombres, les lettres minuscules, les lettres majuscules, et les symboles.</p>
     * <p>Les types de caractères sont définis par des plages de codes Unicode, et le caractère aléatoire est sélectionné en choisissant un type de caractères aléatoire, puis en choisissant un caractère aléatoire dans cette plage.</p>
     * <p>Ensuite, elle construit la chaîne aléatoire en appelant generateRandomCharacter plusieurs fois et en ajoutant chaque caractère généré à une chaîne StringBuilder.</p>
     * <p>Finalement, la méthode renvoie la chaîne construite en appelant la méthode toString de StringBuilder.</p>
     *
     * @return Une chaine composée des SALT_SIZE * un charactère généré aléatoirement
     */
    public synchronized static String generateSalt() {

        StringBuilder salt = new StringBuilder();

        for (int i = 0; i < SALT_SIZE; i++) {
            salt.append(generateRandomCharacter());
        }
        return salt.toString();
    }

    /**
     * Génère un caractère aléatoire en choisissant un type de caractère (nombres, minuscules, majuscules, symboles)
     * aléatoirement et en choisissant un caractère spécifique dans ce type aléatoirement.
     *
     * @return Le caractère généré aléatoirement
     */
    private static char generateRandomCharacter() {

        // 1. Choix aléatoire d'un type de caractère
        int type =  (int)(Math.random() * ALL_CHARACTERS.length);

        // 2. Choix aléatoire d'un caractère dans le type choisi
        int range = (int)(Math.random() * (ALL_CHARACTERS[type][1] - ALL_CHARACTERS[type][0]));
        int selector = (int)(Math.random() * range);
        int character = ALL_CHARACTERS[type][0] + selector;

        // 3. Retour du caractère sous forme de type char
        return (char)character;
    }
}
