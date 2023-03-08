package org.helmo.murmurG6.models;

/*
* CONNECT => 1 = nom_utilisateur
* REGISTER => 1.nom_utilisateur  3.salt_size (4.bcrypt_hash => doit être décomposé)
* FOLLOW => Si tendance : 8. tendance 9. domaine
*        => Si personne : 3. nom_utilisateur 5. domaine
* CONFIRM => 1.sha3_hex
* DISCONNECT => / //TODO Remove ces commentaires ?
* */
public enum TaskType {
     CONNECT, REGISTER, FOLLOW, CONFIRM, DISCONNECT, MSG, MSGS, UNKNOWN
}

