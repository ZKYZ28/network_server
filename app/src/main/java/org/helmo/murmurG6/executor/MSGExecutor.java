package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.models.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MSGExecutor {

    private static final ServerController server = ServerController.getInstance();

    private MSGExecutor() {
    }

    /**
     * Cast un message à des utilisateurs locaux ou distant
     *
     * @param client  Le thread Client Emetteur du message
     * @param message le message à caster
     */
    static void castMsg(ClientRunnable client, String message, String idMessage) {
        //Tactique: on gère d'abord l'envoi des message aux followers du sender,
        // ensuite on gerera les messages aux followers des trends

        System.out.printf("Message envoyé : %s\n", message);
        User sender = client.getUser();

        //Envoi du message au followers de l'emmetteur du message
        castToFollowers(sender, sender.getUserFollowers(), message, idMessage);

        //Si le message fait mention d'au moins 1 trend -> envoi des message aux abonnés de la trend
        if (extractTrends(message).size() > 0) {
            castToTrendFollowers(sender, extractTrends(message), message, idMessage);
        }
    }


    /**
     * Gère le casting de message pour à des followers d'un user
     *
     * @param senderClient L'emetteur du message
     * @param followers    La liste des creditentials des followers (ex: antho123@server1)
     * @param message      Le message à caster
     * @param idMessage    L'id unique associé à ce message
     */
    private static void castToFollowers(User senderClient, Set<UserCredentials> followers, String message, String idMessage) {
        //Parcours de la liste des followers du sender + envoi des message
        for (UserCredentials followerCreditential : followers) {
            manageMessageToClient(senderClient, followerCreditential, idMessage, message);
        }
    }


    /**
     * Gère le casting de message pour à des followers d'une Trend
     *
     * @param senderClient L'emetteur du message
     * @param trends       La liste des trends à gérer
     * @param message      Le message à caster
     * @param idMessage    L'id unique associé à ce message
     */
    private static void castToTrendFollowers(User senderClient, Set<String> trends, String message, String idMessage) {
        //On parcours les trends mentionné dans le message
        for (String trendName : trends) {

            //Si l'emetteur du message est bien abonné à la trend on gère l'envoi, sinon on ne fait rien
            if (senderClient.getTrendByTag(trendName) != null) {
                //On regarde si la trend appartient à ce server
                if (server.getTrendLibrary().exists(trendName)) {
                    TrendLibrary trendLibrary = server.getTrendLibrary();

                    //Si oui, alors on itère sur les followers de cette trends afin de leur écrire
                    for (UserCredentials trendFollowersCredential : trendLibrary.getUsersForTrend(trendName)) {
                        manageMessageToClient(senderClient, trendFollowersCredential, idMessage, message);
                    }


                    //Si non (cas ou la trend n'appartient PAS à ce server) -> on passe la trend au relay
                } else {
                    Trend distantTrend = senderClient.getTrendByTag(trendName);

                    System.out.println(Protocol.build_SEND(idMessage, senderClient.getLogin(), distantTrend.toString(), message));
                    Executor.getInstance().sendToRelay(Protocol.build_SEND(idMessage, senderClient.getCredentials().toString(), distantTrend.toString(), Protocol.build_MSGS(senderClient.getCredentials().toString() + " " + message)));
                }
            }
        }
    }


    private static void manageMessageToClient(User sender, UserCredentials followerCredential, String idMessage, String message) {
        if(!sender.getCredentials().equals(followerCredential)) {
            //on regarde si le destinataire est sur ce server ou sur un autre domaine
            if (followerCredential.getDomain().equals(server.getServerConfig().serverDomain)) {


                //Gere l'envoi du message en local (aux user de CE server)
                operateLocalMessageSend(sender, idMessage, message, followerCredential);


                //Si le destinataire n'appartient pas à ce server
            } else {
                System.out.println("id : " + idMessage);
                System.out.println(Protocol.build_SEND(idMessage, sender.getCredentials().toString(), followerCredential.toString(), message));
                Executor.getInstance().sendToRelay(Protocol.build_SEND(idMessage, sender.getCredentials().toString(), followerCredential.toString(), Protocol.build_MSGS(sender.getCredentials().toString() + " " + message)));
                //TODO a voir avec le toString quand relay fini
            }
        }
    }


    /**
     * Gère l'envoi du message localement (aux utilisateur de CE server)
     *
     * @param sender    L'emetteur du message
     * @param idMessage L'id du message
     * @param message   Le message
     * @param     Le thread ClientRunnable du destinataire sur ce server
     */
    private static void operateLocalMessageSend(User sender, String idMessage, String message, UserCredentials followerCredential) {

        //On recupere le threadClient sur le server du destinataire afin de lui écrire
        ClientRunnable client = server.getClientRunnableByLogin(followerCredential.getLogin());

        //Si le client est connecté
        if (client != null) {

            //On recupere l'objet User associé à ce thread client afin de pourvoir gérer son historique
            User follower = client.getUser();

            //Si le destinataire n'a pas déja recu ce message, alors on lui écrit et enregistre cet événement dans son historique
            if (!follower.hasAlreadyReceivedMessage(idMessage) && !sender.equals(follower)) {
                client.sendMessage(Protocol.build_MSGS(sender.getCredentials().toString() + " " + message));
                follower.saveReceivedMessageId(idMessage);
            }
            //Si le destinataire n'est pas actuellement connecté
        } else {
            //TODO : Extension file de message quand le client n'est pas connecté (quand il est nul dans ce cas ci)
            server.addOfflineMessageForClient(followerCredential, new OffLineMessage(LocalDateTime.now(), message));
        }
    }


    /**
     * Recupere toute les trend (uniquement leur name) mentionnée dans un message
     *
     * @param message le message ananlysé
     * @return un set de tagname
     */
    private static Set<String> extractTrends(String message) {
        Matcher matcher = Pattern.compile(Protocol.TAG).matcher(message);
        HashSet<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group()); //Ex: #trend
        }
        return matches;
    }
}
