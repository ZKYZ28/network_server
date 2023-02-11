package org.helmo.murmurG6.models.exceptions;

/**
 * Classe représentant une exception lorsqu'un utilisateur est déjà enregistré.
 *
 * @since 11 février 2023
 * @version 1.0
 */
public class UserAlreadyRegisteredException extends Exception {

    public UserAlreadyRegisteredException(String msg) {
        super(msg);
    }
}
