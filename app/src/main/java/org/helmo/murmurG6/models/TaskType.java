package org.helmo.murmurG6.models;

/*
* CONNECT => 1 = nom_utilisateur
* REGISTER => 1.nom_utilisateur  3.salt_size (4.bcrypt_hash => doit être décomposé)
* FOLLOW => 1. nom_domaine / tag_domaine
* CONFIRM => 1.sha3_hex
* DISCONNECT => /
* */
public enum TaskType {
     CONNECT, REGISTER, FOLLOW, CONFIRM, DISCONNECT, MSG
}

