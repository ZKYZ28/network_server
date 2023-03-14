package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;


/**
 * La classe RelayThread est responsable de l'écoute des messages envoyés par le relais et de l'envoi des messages à celui-ci pour être transmis aux utilisateurs.<br>
 * La communication entre le serveur et le relais se fait via un socket TCP. Le serveur crée une connexion TCP avec le relais et envoie des messages à celui-ci pour être transmis aux utilisateurs.<br>
 * En ce qui concerne le message ECHO, il est envoyé périodiquement toutes les 15 secondes par le serveur en multicast via un socket Datagram UDP.<br>
 * Le message ECHO est utilisé pour informer tous le relay que le serveur est en ligne et prêt à se connecté à celui-ci.
 */
public class RelayThread implements Runnable, AutoCloseable {

    private static final long ECHO_FREQUENCY = 1;
    private final ServerConfig config;
    private ServerSocket serverSocket;
    private MulticastSocket multicastSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ScheduledFuture<?> echoTask;


    /**
     * Constructeur de la classe RelayThread.<br>
     * Ce constructeur crée un socket serveur avec le port 0 pour utiliser un port libre aléatoire et un socket datagramme pour l'envoi de messages "echo". <br>
     * Le socket serveur sera utilisé pour écouter les connexions entrantes et le socket datagramme sera utilisé pour envoyer des messages aux clients. <br>
     * Les informations du serveur, telles que la clé AES, le nom de domaine et les ports utilisés, sont fournies via l'objet ServerController passé en paramètre.<br>
     *
     * @param server Le serveur hôte utilisé pour la réception des informations du serveur (clé AES, nom de domaine, ports utilisés...)<br>
     */
    public RelayThread(ServerController server) {
        this.config = server.getServerConfig();
        try {
            this.serverSocket = new ServerSocket(0);
            this.multicastSocket = new MulticastSocket();
            multicastSocket.setNetworkInterface(config.networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Envoie un message chiffré au serveur de relais via la connexion de sortie.<br>
     * Le message est d'abord chiffré en utilisant l'algorithme de chiffrement AES avec la clé spécifiée dans la configuration du serveur.<br>
     * Le message chiffré est ensuite encodé en Base64 avant d'être envoyé au serveur de relais via la connexion de sortie.<br>
     *
     * @param sendMessage Le message à envoyer au serveur de relais
     */
    public void sendToRelay(String sendMessage) {
        try {
            byte[] ciphertext = AESCrypt.encrypt(sendMessage, config.base64KeyAES);
            String ciphertext_base64 = Base64.getEncoder().encodeToString(ciphertext);
            out.println(ciphertext_base64);
            out.flush();
            System.out.println("[RelayThread] Message envoyé au relay : " + sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Reçoit un message chiffré en provenance du serveur de relais via la connexion d'entrée.<br>
     * Le message est d'abord reçu sous forme de chaîne de caractères, puis il est décodé en Base64 avant d'être déchiffré en utilisant l'algorithme de chiffrement AES avec la clé spécifiée dans la configuration du serveur.<br>
     *
     * @return Le message déchiffré en provenance du serveur de relais, ou null en cas d'erreur
     */
    public String receiveFromRelay() {
        try {
            String line = in.readLine();
            byte[] decodedBytes = Base64.getDecoder().decode(line);
            String decryptedMessage = AESCrypt.decrypt(decodedBytes, config.base64KeyAES);
            System.out.println("[RelayThread] Message en provenance du relay reçu : " + decryptedMessage);
            return decryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Envoie un paquet de données (DatagramPacket) à l'adresse IP de diffusion multicast à travers le DatagramSocket.<br>
     * Le contenu du paquet est une chaîne de caractères qui contient le port local du serveur et le nom de domaine du serveur pour que le relay sache comment s'y connecter.
     */
    public void echo() {
        try {
            String echoMessage = "ECHO " + this.serverSocket.getLocalPort() + " " + config.serverDomain + "\r\n";
            byte[] msgsBytes = echoMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(msgsBytes, msgsBytes.length, InetAddress.getByName(config.multicastIp), config.multicastPort);
            this.multicastSocket.send(packet);
            System.out.println("[RelayThread] " + echoMessage);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }


    /**
     * La méthode listen implémente une boucle d'écoute pour la communication unicast avec un relay en configurant les flux d'entrée/sortie. <br>
     * Elle annule le thread "echo" dès la connection du relay car une fois connecté, il n'y a plus d'utilité à envoyer le echo. <br>
     * Elle sert également à recevoir et traiter les messages reçus via les méthodes receiveFromRelay() et handleReceivedMessage() <br>
     */
    public void listen() {
        try {
            Socket unicastSocket = this.serverSocket.accept(); //Connection avec le relay
            this.in = new BufferedReader(new InputStreamReader(unicastSocket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(new OutputStreamWriter(unicastSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            //Relay connecté donc plus besoin de faire echo, fermeture du multicast socket et annulation du thread echo
            cancelEcho();

            String message = receiveFromRelay();

            while (!unicastSocket.isClosed() && !message.isEmpty()) {
                handleReceivedMessage(message);
                message = receiveFromRelay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gère un message reçu en analysant s'il s'agit d'une tâche de type SEND selon le protocole spécifié.<br>
     * Si oui, crée une tâche correspondante et l'ajoute à l'executor.
     *
     * @param message le message à traiter
     */
    private void handleReceivedMessage(String message) {
        Executor executor = Executor.getInstance();
        Matcher matcher = Protocol.getMatcher(TaskType.SEND, message);

        if (matcher != null && matcher.matches()) {
            //Mise en place du UserCreditential de l'emetteur
            String senderDomain = matcher.group("sender");
            Matcher senderParams = Protocol.RX_USER_DOMAIN.matcher(senderDomain);
            if (senderParams.matches()) {
                UserCredentials senderCreditential = new UserCredentials(senderParams.group("login"), senderParams.group("userServerDomain"));
                Task task = new Task(matcher.group("id"), senderCreditential, matcher.group("receiver"), Protocol.detectTaskType(matcher.group("content")), matcher.group("content"));
                System.out.println("[RelayThread] Tâche ajoutée: " + task.getTaskId() + " " + task.getSender() + " " + task.getReceiver() + " " + task.getType() + " " + task.getContent());
                executor.addTask(task);
            }
        }
    }


    /**
     * Ferme le socket multicast et annule le thread responsable du echo
     */
    private void cancelEcho() {
        this.multicastSocket.close();
        this.echoTask.cancel(true);
        System.out.println("[RelayThread] Annulation du thread echo car connection avec Relay établie.");
    }


    /**
     * Lance les 2 thread principaux, un pour envoyé echo périodiquement et l'autre pour écouter les messages entrants provenant du Relay.<br>
     * Utilise un ScheduledThreadPool de 2 threads. Un thread est utilisé pour envoyer périodiquement un message ECHO
     * toutes les 15 secondes, tandis que l'autre thread est utilisé pour écouter les messages entrants provenant du relais.
     */
    @Override
    public void run() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        this.echoTask = executorService.scheduleAtFixedRate(this::echo, 0, ECHO_FREQUENCY, TimeUnit.SECONDS);
        executorService.submit(this::listen);
    }


    /**
     * Fermeture des sockets, du BufferedReader et du PrintWriter.
     */
    @Override
    public void close() {
        try {
            this.serverSocket.close();
            this.multicastSocket.close();
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}