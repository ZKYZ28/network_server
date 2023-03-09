package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;


/**
 * La classe RelayThread est responsable de l'écoute des messages envoyés par le relais et de l'envoi des messages à celui-ci pour être transmis aux utilisateurs.
 * Elle permet également au server de s'annoncer toutes les 15 secondes via le message ECHO
 */
public class RelayThread implements Runnable, AutoCloseable {

    private final ServerConfig config;
    private ServerSocket serverSocket;
    private DatagramSocket multicastSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ScheduledFuture<?> echoTask;


    public RelayThread(ServerController server) {
        this.config = server.getServerConfig();
        try {
            this.serverSocket = new ServerSocket(0);
            this.multicastSocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie un message chiffré au relais pour être transmis aux utilisateurs.
     *
     * @param sendMessage le message à envoyer
     */
    public void sendToRelay(String sendMessage) {
        try {
            System.out.println("Envois au relay du message: " + sendMessage);


            byte[] ciphertext = AESCrypt.encrypt(sendMessage, "DHADoCxPItcFyKwxcTEuGg5neBd2K+VLXWc6zCnsBq4=");
            String ciphertext_base64 = Base64.getEncoder().encodeToString(ciphertext);

            System.out.println("Envois du message chiffré : " + ciphertext_base64);

            out.println(ciphertext_base64); // Send the encrypted message to the relay
            out.flush();
            System.out.println("[RelayThread] Envoi d'un message au relay.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reçoit un message du relais.
     *
     * @return le message reçu
     */
    public String receiveFromRelay() {
        try {
            String line = in.readLine();
            System.out.println("RELAY : " + line);
            byte[] decodedBytes = Base64.getDecoder().decode(line);
            return AESCrypt.decrypt(decodedBytes, config.base64KeyAES);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie un message ECHO en multicast pour signaler la disponibilité du serveur au Relay.
     */
    public void echo() {
        try {
            String echoMessage = "ECHO " + this.serverSocket.getLocalPort() + " " + config.serverDomain + "\r\n";
            byte[] msgsBytes = echoMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(msgsBytes, msgsBytes.length, InetAddress.getByName(config.multicastIp), config.multicastPort);
            this.multicastSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Écoute les messages reçus du relais et les traite en ajoutant les tâches correspondantes à l'Executor.
     */
    public void listen() {
        try {
            Socket unicastSocket = this.serverSocket.accept(); //Connection avec le relay
            this.in = new BufferedReader(new InputStreamReader(unicastSocket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(new OutputStreamWriter(unicastSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            //Relay connecté donc plus besoin de faire echo, fermeture du multicast socket et annulation du thread echo
            cancelEcho();

            String message = receiveFromRelay();

            System.out.println("MESSAGE DECHIFFRE : " + message);

            while (!unicastSocket.isClosed() && !message.isEmpty()) {
                handleReceivedMessage(message);
                message = receiveFromRelay();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Traite le message reçu du relais en extrayant les informations de la tâche associée et en l'ajoutant à l'exécuteur.
     *
     * @param message le message reçu du relais
     */
    private void handleReceivedMessage(String message) {
        Executor executor = Executor.getInstance();
        Matcher matcher = Protocol.getMatcher(TaskType.SEND, message);

        if (matcher != null && matcher.matches()) {
            //Mise en place du UserCreditential de l'emetteur
            String senderDomain = matcher.group("sender"); //ex: "antho123@serv2.godswila.guru"
            Matcher senderParams = Protocol.RX_USER_DOMAIN.matcher(senderDomain);
            if(senderParams.matches()){
                UserCredentials senderCreditential = new UserCredentials(senderParams.group("login"), senderParams.group("userServerDomain"));
                Task task = new Task(matcher.group("id"),  senderCreditential, matcher.group("receiver"), Protocol.detectTaskType(matcher.group("content")), matcher.group("content"));

                System.out.println("Tache ajoutée: " + task.getTaskId()+" "+task.getSender()+" "+task.getReceiver()+" "+task.getType()+" "+task.getContent());
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

    /**
     * Lance les 2 thread principaux, un pour envoyé echo périodiquement et l'autre pour écouter les messages entrants provenant du Relay.
     * Utilise un ScheduledThreadPool de 2 threads. Un thread est utilisé pour envoyer périodiquement un message ECHO
     * toutes les 15 secondes, tandis que l'autre thread est utilisé pour écouter les messages entrants provenant du relais.
     */
    @Override
    public void run() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        this.echoTask = executorService.scheduleAtFixedRate(this::echo, 0, 15, TimeUnit.SECONDS);
        executorService.submit(this::listen);
    }
}