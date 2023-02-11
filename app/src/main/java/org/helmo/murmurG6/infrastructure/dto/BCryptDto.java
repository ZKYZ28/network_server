package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;

/**
 * Classe Data Transfer Object pour les informations du hachage Bcrypt.
 * Cette classe est utilisée pour la conversion des informations du hachage Bcrypt en objet BcryptHash et inversement.
 *
 * @version 1.0
 * @since 11 février 2023
 */
public class BCryptDto {

    public  int rounds; //Nombre de tours pour le hachage Bcrypt
    public String salt; //La sel associé au hachage Bcrypt
    public String hash; //Le hachage Bcrypt


    /**
     * Convertit un objet BcryptHashDto en objet BcryptHash.
     *
     * @param dto l'objet BcryptHashDto à convertir
     * @return l'objet BcryptHash converti
     */
    public static BCrypt bcryptDtoToBcrypt(BCryptDto dto){
       return new BCrypt(dto.rounds, dto.salt, dto.hash);
    }
}
