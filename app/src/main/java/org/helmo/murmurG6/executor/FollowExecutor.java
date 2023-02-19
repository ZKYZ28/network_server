package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.controller.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.controller.exceptions.UnableToFollowUserException;
import org.helmo.murmurG6.models.Trend;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

import java.util.regex.Matcher;

public class FollowExecutor {

    private static final ServerController server = ServerController.getInstance();

    protected static void follow(ClientRunnable client, String target) {
        User user = client.getUser();
        try {
            if (target.startsWith("#")) {
                followTrend(user, target);
            } else {
                followUser(user, target);
            }
            server.save();
        } catch (UnableToSaveTrendLibraryException | UnableToSaveUserLibraryException e) {
            e.printStackTrace();
        }
    }

    private static void followUser(User user, String userToFollow) throws UnableToFollowUserException {
        Matcher matcher = Protocol.RX_USER_DOMAIN.matcher(userToFollow);
        if (matcher.matches()) {
            String login = matcher.group("login");
            String domain = matcher.group("userServerDomain");
            if (!domain.equals(server.getServerConfig().getServerName())) {
                Protocol.build_SEND("", "", "", "");
            } else {
                UserLibrary userLibrary = server.getUserLibrary();

                if (userLibrary.isRegistered(login)) {
                    User followedUser = userLibrary.getUser(login);
                    followedUser.addFollower(user.getCredentials());
                }
            }
        }
    }

    private static void followTrend(User user, String trendToFollow) throws UnableToFollowUserException {
        Matcher matcher = Protocol.TAG_DOMAIN.matcher(trendToFollow);
        if (matcher.matches()) {
            String trendName = matcher.group("tagName");
            String domain = matcher.group("trendServerDomain");
            user.followTrend(new Trend(trendName, domain));

            if (!domain.equals(server.getServerConfig().getServerName())) {
                Protocol.build_SEND("sdfsdf", "sdfsdf", "sdfsdf", "sdfsdf");
            } else {
                server.getTrendLibrary().addUserToTrend(trendName, user.getCredentials());
            }
        }
    }
}
