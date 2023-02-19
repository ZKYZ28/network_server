package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;

public class BCryptDto {
    public int rounds;
    public String salt;
    public String hash;

    public static BCrypt fromDto(BCryptDto dto) {
        return new BCrypt(dto.rounds, dto.salt, dto.hash);
    }

    public static BCryptDto toDto(BCrypt bcrypt) {
        BCryptDto bc = new BCryptDto();
        bc.salt = bcrypt.getSalt();
        bc.rounds = bcrypt.getRounds();
        bc.hash = bcrypt.getHash();
        return bc;
    }
}
