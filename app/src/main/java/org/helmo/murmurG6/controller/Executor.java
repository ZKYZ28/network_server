package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.controller.exceptions.UnableToFollowUserException;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;
import org.helmo.murmurG6.utils.RandomSaltGenerator;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements TaskScheduler {

    private static Executor instance;
    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue;   //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private ServerController server;

    private Executor() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (server.isRunning()) {
                try {
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread (waiting) jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    throw new UnableToExecuteTaskException("Une erreur est survenue lors de l'exécution de la tâche", e);
                }
            }
        });
    }


    private void executeTask(Task task) {

        ClientRunnable client = task.getClient(); //On récupère le client à qui on fait la tache
        Matcher params = task.getMatcher();       //On récupère le matcher de la tache à éxécuter
        User user;

        switch (task.getType()) {
            case REGISTER:
                user = new User(new UserCredentials(params.group("username"), server.getServerConfig().getServerName()), BCrypt.of(params.group("bcrypt")), new HashSet<>(), new HashSet<>());
                client.sendMessage(register(user, client));
                break;

            case CONNECT:
                user = server.getUserLibrary().getUser(params.group("username"));
                client.setUser(user);
                client.sendMessage(connect(user.getUserCredentials()));
                break;

            case CONFIRM:
                user = client.getUser();
                String received = params.group("challenge");
                String expected = user.getBcrypt().generateChallenge(client.getRandom22());
                client.sendMessage(confirm(received, expected));
                break;

            case MSG:
                server.castMsg(client.getUser(), params.group("message"));
                break;

            case FOLLOW:
                follow(client.getUser(), params.group("domain"));
                break;

            case DISCONNECT:
                //TODO retirer le client de la clientlist

            default:
                client.sendMessage(Protocol.build_ERROR());
                break;
        }
    }

    public String sayHello(ClientRunnable client) {
        String random22 = RandomSaltGenerator.generateSalt();
        client.sendMessage(Protocol.build_HELLO(server.getServerConfig().getServerName(), random22));
        return random22;
    }

    private String register(User user, ClientRunnable client) {
        try {
            server.getUserLibrary().register(user);
            client.setUser(user);
            server.save();
            return Protocol.build_OK();
        } catch (UnableToSaveUserLibraryException | UnableToSaveTrendLibraryException | UserAlreadyRegisteredException e) {
            return Protocol.build_ERROR();
        }
    }

    private String connect(String login) {
        if (server.getUserLibrary().isRegistered(login)) {
            User user = server.getUserLibrary().getUser(login);
            return Protocol.build_PARAM(user.getBcryptRound(), user.getBcryptSalt());
        } else {
            return Protocol.build_ERROR();
        }
    }

    private String confirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? Protocol.build_OK() : Protocol.build_ERROR();
    }

    private void follow(User user, String target) {
        try {
            if (target.startsWith("#")) {
                followTrend(user, target);
            } else {
                followUser(user, target);
            }
            this.server.save();
        } catch (UnableToSaveTrendLibraryException | UnableToSaveUserLibraryException e) {
            //TODO TREATMENT
            e.printStackTrace();
        }
    }

    private void followUser(User user, String userToFollow) throws UnableToFollowUserException {
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

    private void followTrend(User user, String trendToFollow) throws UnableToFollowUserException {
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

    @Override
    public void close() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }


    /********** GETTERS/SETTERS ************/

    public static Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
        }
        return instance;
    }

    public void setServer(ServerController server) {
        this.server = server;
    }

    public void sendToRelay(String sendMessage) {

    }
}