package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.utils.RandomSaltGenerator;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements TaskScheduler {

    private static Executor instance;
    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private ServerController server;

    private Executor() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
        }
        return instance;
    }

    public void setServer(ServerController server) {
        this.server = server;
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (true) {
                try {
                    //L'executor bloque ici jusqu'à ce qu'une nouvelle tache arrvie
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }


    private void executeTask(Task task) {

        ClientRunnable client = task.getClient(); //On récupère le client à qui on fait la tache
        Matcher params = task.getMatcher();   //On récupère le matcher de la tache à éxécuter
        User user;

        switch (task.getType()) {
            case REGISTER:
                user = new User(params.group("username"), BCrypt.of(params.group("bcrypt")), new HashSet<>(), new HashSet<>());
                client.sendMessage(register(user, client));
                break;

            case CONNECT:
                user = server.getUserCollection().get(params.group("username"));
                client.setUser(user);
                client.sendMessage(connect(user.getLogin()));
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
                try {
                    follow(client.getUser(), params.group("domain"));
                    server.saveUsers();
                } catch (SaveUserCollectionException | UnableToFollowUser e) {
                    System.out.println(e.getMessage());
                    client.sendMessage(Protocol.build_ERROR());
                }
                break;


            case DISCONNECT:
                //TODO retirer le client de la clientlist

            default:
                client.sendMessage(Protocol.build_ERROR());
        }
    }

    private String connect(String login) {
        if (server.getUserCollection().containsKey(login)) {
            User user = server.getUserCollection().get(login);
            return Protocol.build_PARAM(user.getBcryptRound(), user.getBcryptSalt());
        } else {
            return Protocol.build_ERROR();
        }
    }

    private String confirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? Protocol.build_OK() : Protocol.build_ERROR();
    }

    private String register(User user, ClientRunnable client) {
        try {
            server.getUserCollection().register(user);
            client.setUser(user);
            server.saveUsers();
            return Protocol.build_OK();
        } catch (SaveUserCollectionException | UserAlreadyRegisteredException e) {
            return Protocol.build_ERROR();
        }
    }

    public String sayHello(ClientRunnable client) {
        String random22 = RandomSaltGenerator.generateSalt();
        client.sendMessage(Protocol.build_HELLO(server.getIp(), random22));
        return random22;
    }


    //test@server1 || #test@server2
    private void follow(User user, String itemToBeFollowed) throws UnableToFollowUser {
        Matcher matcher = Protocol.TAG_DOMAIN_OR_RX_USER_DOMAIN.matcher(itemToBeFollowed);
        if (!matcher.matches()) {
            return;
        }

        String domain = matcher.group("UserServerDomain");
        if (!domain.equals(server.getServerConfig().getServerName())) {
            Protocol.build_SEND("", "", "", "");
        } else {
            String login = matcher.group("login");
            String userDomain = user.getLogin() + "@" + server.getServerConfig().getServerName();

            if (itemToBeFollowed.charAt(0) == '#') {
                user.followTrend(itemToBeFollowed);
            } else {
                User followedUser = server.getUserCollection().get(login);
                followedUser.addFollower(userDomain);
            }
        }
    }


    //TODO: Gérer le cas ou le login a suivre n'existe pas (checker sur TOUS les servers)
   /* private void followUser(User user, String loginToBeFollowed) throws UnableToFollowUser{
        Matcher matcher = Protocol.RX_USER_DOMAIN.matcher(loginToBeFollowed);
        if(matcher.matches() && matcher.group("login").equals(user.getLogin())){
            throw new UnableToFollowUser("login d'utilisateur à suivre invalide!");
        }else{
            user.followUser(loginToBeFollowed);
        }
    }*/




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
}