package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.BcryptHash;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;

import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements Runnable, AutoCloseable {

    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.

    private final ServerController server; //Le server qui possède l'executor

    public Executor (ServerController server) {
        taskQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newSingleThreadExecutor();
        this.server = server;
    }

    /**
     * Permet d'ajouter des tâches à la file d'attente
     *
     * @param task La tâche a ajouter à la file d'attente
     */
    public void addTask(Task task) {
        taskQueue.add(task);
    }

    /**
     * Fait tourner l'executor sur un thread
     */
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


    /**
     * Execute la tache passée en paramètre selon son type
     * @param task la tache à exécuter par l'éxécutor
     */
    public void executeTask(Task task) {

        ClientRunnable client = task.getClient(); //On récupère le client à qui on fait la tache
        Matcher params = task.getMatcher();   //On récupère le matcher de la tache à éxécuter

        switch (task.getType()){
            case REGISTER:
                try{
                    User u = new User(params.group(1), BcryptHash.decomposeHash(params.group(4))); //Construit un User sur base des élément de la commande REGISTER
                    server.registerUser(u); //Enregistre l'utilisateur sur le server
                    client.setUser(u);      //Associe un objet User à un ClientRunnable
                }catch (RegistrationImpossibleException e){
                    System.out.println(e.getMessage());
                }
                break;

            case CONNECT:
                User u = server.getUserCollection().getUserOnLoggin(params.group(1)); //Recupere l'utilisateur dans la liste des inscrits sur base de son loggin
                client.setUser(u);  //Associe un objet User à un ClientRunnable
                client.sendMessage(connect(params.group(1), client.getUser().getBcryptSalt())); //Envoi du message PARAM au client
                break;

            case CONFIRM:
                //Envoi du message +OK ou -ERR selon si la connexion s'est déroulée avec succès ou non
                client.sendMessage(confirm(params.group(1), client.getUser().getBcrypt().calculateChallenge(client.getRandom22())));
                break;
        }
    }


    /**
     * Retourne le message à envoyer au client lorsque celui veut se connecter
     * @param loggin le loggin de l'utilisateur qui veut se connecter
     * @param salt le sel utilisé par le client
     * @return le message PARAM oubien -ERR
     */
    private String connect(String loggin, String salt){
        if (server.getUserCollection().isRegistered(loggin)) {
            return "PARAM " + server.getUserCollection().getRegisteredUsers().get(loggin).getBcryptRound() + " " +salt;
        } else {
            return  "-ERR";
        }
    }


    /**
     * Retourne la confirmation ou non de la connexion du client
     * @param clientChallenge le challenge calculé par le client
     * @param userChallenge le challenge calculé par le server
     * @return le message "+OK" si le challenge-client correspond au challenge-server. "-ERR" sinon
     */
    private String confirm(String clientChallenge, String userChallenge){
        return clientChallenge.equals(userChallenge) ? "+OK" : "-ERR";
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


}
