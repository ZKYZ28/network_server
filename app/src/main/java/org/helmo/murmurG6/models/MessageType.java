package org.helmo.murmurG6.models;



/*
* CONNECT => 1 = nom_utilisateur
* REGISTER => 1.nom_utilisateur  3.salt_size 4.bcrypt_hash
* FOLLOW =>
* CONFIRM => 1.sha3_hex
* DISCONNECT => /
* */
public enum MessageType {
     CONNECT, REGISTER, FOLLOW, CONFIRM, DISCONNECT, MESSAGE;
}

