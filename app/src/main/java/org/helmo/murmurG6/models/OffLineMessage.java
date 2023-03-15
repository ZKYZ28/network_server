package org.helmo.murmurG6.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class OffLineMessage{

    private String messageId;
    private final String senderCreditentialsInString;
    private final LocalDateTime dateTime;
    private String message;

    public OffLineMessage(String senderCreditentialsInString, LocalDateTime dateTime, String message){
        this.senderCreditentialsInString = senderCreditentialsInString;
        this.dateTime = dateTime;
        this.message = message;
    }


    public String getMessageId(){
        return this.messageId;
    }
    public String getSenderCreditentialsInString() {
        return senderCreditentialsInString;
    }
    public String getDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
    public String getMessage() {
        return message;
    }


    /*
    Pour la création de l'id, il fallait opter pour un moyen d'obtenir un clé triable par date (plus ancien au plus récent).
    Tout chiffrement de cet id pertureberait l'ordre du tri. Par conséquent, on crée un id qui est le resultat sous forme de
    chaine de caractères de la concaténation de la date de traitement du message avec l'emetteur de ce message.
    Créer un id uniquement sur base de la date aurait été problématique dans le cas ou le client recevrait deux message en
    même temps.
    Dans notre cas, on considèrera que seule l'information "message" est une information sensible à sécuriser. La date et l'émetteur
    étant des informations non sensibles car les information d'utilisateur sont stockée en claire dans le serveur.
     */
    public void setEncryptedMessage(String ciphertext_base64) {
        this.message = ciphertext_base64;
        this.messageId = (int) dateTime.toEpochSecond(ZoneOffset.UTC)+senderCreditentialsInString;
    }
}
