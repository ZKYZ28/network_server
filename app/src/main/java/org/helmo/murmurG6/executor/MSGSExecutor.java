package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.models.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;

public final class MSGSExecutor {

    private static final ServerController server = ServerController.getInstance();

    private MSGSExecutor() {
    }

    static void castMsgs(Task task, String message) {
        Matcher params = Protocol.TAG_DOMAIN_OR_RX_USER_DOMAIN.matcher(task.getReceiver());
        if (params.matches()) {
            if (task.getReceiver().charAt(0) == '#') {
                //On envoi le message à tous les followers de la trend (ici la trend appartient au server)
                sendMessageToFollowerOfTrend(task.getSender(), task.getReceiver(), message, task.getTaskId());

            } else {
                //On envoi le message à 1 client (le "receiver")
                sendMessageToReceiverClient(task.getSender(), task.getReceiver(), message, task.getTaskId());
            }
        }

    }

    private static void sendMessageToFollowerOfTrend(UserCredentials senderCreditentials, String target, String message, String messageId) {
        Matcher targetArgs = Protocol.TAG_DOMAIN.matcher(target);

        if (targetArgs.matches()) {
            //On itère sur tous les followers de la trend appartenant au serveur
            for (UserCredentials trendFollowerCreditentials : server.getTrendLibrary().getUsersForTrend(targetArgs.group("tagName"))) {

                //Le sender ne doit pas s'envoyer de message à lui meme
                if(!senderCreditentials.equals(trendFollowerCreditentials)) {
                    //Si le destinataire du message appartient à ce serveur
                    if (server.getUserLibrary().isRegistered(trendFollowerCreditentials.getLogin())) {

                        sendMessageToReceiverClient(senderCreditentials, trendFollowerCreditentials.toString(), message, messageId);

                        //Cas ou le destinataire appartient à un autre serveur
                    } else {
                        Executor.getInstance().sendToRelay(Protocol.build_SEND(messageId, senderCreditentials.toString(), trendFollowerCreditentials.toString(), Protocol.build_MSGS(senderCreditentials.toString() + " " + message)));
                    }
                }
            }
        }
    }

    private static void sendMessageToReceiverClient(UserCredentials sender, String receiver, String message, String messageId) {
        Matcher receiverArgs = Protocol.RX_USER_DOMAIN.matcher(receiver);

        //Si le format et la syntaxe du destinataire est correcte
        if (receiverArgs.matches()) {
            User receiverUser = server.getUserLibrary().getUser(receiverArgs.group("login"));
            ClientRunnable client = server.getClientRunnableByLogin(receiverUser.getLogin());

            //On opère l'envoi du message au destinataire
            operateLocalMessageSend(sender, message, messageId, receiverUser, client);
        }
    }


    private static void operateLocalMessageSend(UserCredentials sender, String message, String messageId, User receiverUser, ClientRunnable client) {
        //Si le destinataire n'a pas déja recu le message, on lui envoi
        if (!receiverUser.hasAlreadyReceivedMessage(messageId)) {

            //Si le client est actuellement connecté
            if (client != null) {

                //On lui écrit et lui fait enregistrer dans son historique de reception
                client.sendMessage(Protocol.build_MSGS(sender.toString() + " " + message));
                receiverUser.saveReceivedMessageId(messageId);


                //Cas ou il n'est pas actuellement connecté
            } else {
                //TODO ajout dans la file de message hors ligne
                System.out.println("ajout du message hors ligne");
                try{
                    byte[] ciphertext = AESCrypt.encrypt(message, server.getServerConfig().base64KeyAES);
                    String ciphertext_base64 = Base64.getEncoder().encodeToString(ciphertext);
                    server.addOfflineMessageForClient(receiverUser.getCredentials(), new OffLineMessage(sender.toString(), LocalDateTime.now(), ciphertext_base64));
                }catch (Exception e){
                    System.out.println("ERREUR lors de l'encryptage du message hors ligne");
                }
            }
        }
    }
}
