package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.controller.exceptions.UnableToFollowUserException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

import java.util.regex.Matcher;

public class FollowExecutor {

    private static final ServerController server = ServerController.getInstance();

    protected static void follow(UserCredentials senderCredentials, String target) {
        try {
            if (target.startsWith("#")) {
                followTrend(senderCredentials, target);
            } else {
                followUser(senderCredentials, target);
            }
            server.save();
        } catch (UnableToSaveTrendLibraryException | UnableToSaveUserLibraryException e) {
            e.printStackTrace();
        }
    }

    private static void followUser(UserCredentials senderCreditentials, String userToFollow) throws UnableToFollowUserException {
        Matcher matcher = Protocol.RX_USER_DOMAIN.matcher(userToFollow);
        if (matcher.matches()) {
            String login = matcher.group("login");
            String domain = matcher.group("userServerDomain");

            //Si la target n'est pas sur ce server, on envoi au relay
            if (!domain.equals(server.getServerConfig().getServerName())) {
                Protocol.build_SEND("", "", "", "");

            //Si domain == le domaine de ce server
            } else {
                UserLibrary userLibrary = server.getUserLibrary();

                //On vérrifie si target existe sur le server
                if (userLibrary.isRegistered(login)) {
                    User followedUser = userLibrary.getUser(login);
                    followedUser.addFollower(senderCreditentials);
                }
            }
        }
    }

    private static void followTrend(UserCredentials senderCreditentials, String trendToFollow) throws UnableToFollowUserException {
        Matcher matcher = Protocol.TAG_DOMAIN.matcher(trendToFollow);
        if (matcher.matches()) {
            String trendName = matcher.group("tagName");
            String domain = matcher.group("trendServerDomain");
            Trend trend = new Trend(trendName, domain);

            //USER
            //Cas ou le sender appartient au server et y est inscrit
            if(senderCreditentials.getDomain().equals(server.getServerConfig().getServerName()) && server.getUserLibrary().isRegistered(senderCreditentials.getLogin())) {

                //On récupère ce sender
                User user = server.getUserLibrary().getUser(senderCreditentials.getLogin());

                //On ajoute la trend dans sa liste de trend followed
                user.followTrend(trend);
            }



            //TREND
            //Si la trend appartient au server on enregistre
            if(trend.getDomain().equals(server.getServerConfig().getServerName())){
                server.getTrendLibrary().addUserToTrend(trendName, senderCreditentials);

            //Si la trend appartient a un autre server
            }else{
                Executor.getInstance().sendToRelay(Protocol.build_SEND("sdfsdf", "sdfsdf", "sdfsdf", "sdfsdf"));
            }
        }
    }
}
