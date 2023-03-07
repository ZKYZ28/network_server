package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.AESCrypt;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.UserCredentials;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;


/**
 * La classe RelayThread est responsable de l'écoute des messages envoyés par le relais et de l'envoi des messages à celui-ci pour être transmis aux utilisateurs.
 * Elle permet également au server de s'annoncer toutes les 15 secondes via le message ECHO
 */
public class RelayThread implements Runnable, AutoCloseable {

    private static final String MULTICAST_IP = "224.1.1.255";
    private static final int MULTICAST_PORT = 23106;
    private static final int RELAY_PORT = 20201;

    private ServerSocket serverSocket;
    private DatagramSocket multicastSocket;
    private BufferedReader in;
    private PrintWriter out;
    private final ServerController server;


    public RelayThread(ServerController server) {
        this.server = server;
        try {
            this.serverSocket = new ServerSocket(RELAY_PORT);
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
            byte[] msgsBytes = AESCrypt.encrypt(sendMessage, server.getServerConfig().getBase64KeyAES());
            out.println(Arrays.toString(msgsBytes));
            out.flush();
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
            return AESCrypt.decrypt(line.getBytes(), server.getServerConfig().getBase64KeyAES());
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
            String echoMessage = "ECHO " + RELAY_PORT + " " + server.getServerConfig().getServerName() + "\r\n";
            byte[] msgsBytes = echoMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(msgsBytes, msgsBytes.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
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
            Socket unicastSocket = this.serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(unicastSocket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(new OutputStreamWriter(unicastSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.multicastSocket.close();

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
     * Traite le message reçu du relais en extrayant les informations de la tâche associée et en l'ajoutant à l'exécuteur.
     *
     * @param message le message reçu du relais
     */
    private void handleReceivedMessage(String message) {
        Executor executor = Executor.getInstance();
        Matcher args = Protocol.RX_SEND.matcher(message);

        if (args.matches()) {
            //Mise en place du UserCreditential de l'emetteur
            String senderDomain = args.group("sender"); //ex: "antho123@serv2.godswila.guru"
            Matcher senderParams = Protocol.RX_USER_DOMAIN.matcher(senderDomain);
            UserCredentials senderCreditential = new UserCredentials(senderParams.group("login"), senderParams.group("domain"));

            Task task = new Task(args.group("id"), null, senderCreditential, args.group("receiver"), Protocol.detectTaskType(args.group("content")), args.group("content"));

            executor.addTask(task);
        }
    }


    @Override
    public void close() {

    }

    /**
     * Lance les 2 thread principaux, un pour envoyé echo périodiquement et l'autre pour écouter les messages entrants provenant du Relay.
     * Utilise un ScheduledThreadPool de 2 threads. Un thread est utilisé pour envoyer périodiquement un message ECHO
     * toutes les 15 secondes, tandis que l'autre thread est utilisé pour écouter les messages entrants provenant du relais.
     */
    @Override
    public void run() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

        executorService.scheduleAtFixedRate(this::echo, 0, 15, TimeUnit.SECONDS);
        executorService.submit(this::listen);
    }
}