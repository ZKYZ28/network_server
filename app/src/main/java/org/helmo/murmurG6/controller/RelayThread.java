package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.AESCrypt;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.UserCredentials;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class RelayThread implements Runnable, AutoCloseable {

    private final int PORT = 19375;
    private final ScheduledExecutorService executorService;
    private InetAddress multicastAddress;
    private InetSocketAddress group;
    private MulticastSocket multicastSocket;
    private ServerConfig config;


    public RelayThread() {
        executorService = Executors.newScheduledThreadPool(2);

        try {
            String NETWORK_INTERFACE = "eth12";
            NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);

            String MULTICAST_IP = "224.1.1.255";
            this.multicastAddress = InetAddress.getByName(MULTICAST_IP);
            this.multicastSocket = new MulticastSocket(PORT);
            this.group = new InetSocketAddress(multicastAddress, PORT);

            this.multicastSocket.joinGroup(group, networkInterface);
        } catch (IOException e) {
            //throw new UnableToCreateRelayThreadException("An error has occured when trying to initialize the connection with the MurmurRelay.", e);
        }
    }


    public void init(ServerController server) {
        this.config = server.getServerConfig();
    }


    public void sendToRelay(String sendMessage) {
        try {
            byte[] msgsBytes = AESCrypt.encrypt(sendMessage, config.getBase64KeyAES());
            DatagramPacket packet = new DatagramPacket(msgsBytes, msgsBytes.length, this.group);
            this.multicastSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String receiveFromRelay() {
        try {
            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.multicastSocket.receive(packet);
            return AESCrypt.decrypt(packet.getData(), config.getBase64KeyAES());
        } catch (Exception e) {
            return null;
        }
    }

    public void echo() {
        try {
            String echoMessage = "ECHO " + PORT + " " + config.getServerName();
            byte[] msgsBytes = echoMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(msgsBytes, msgsBytes.length, this.group);
            this.multicastSocket.send(packet);
        } catch (IOException e) {
            //
        }
    }

    public void listen() {
        Executor executor = Executor.getInstance();

        while (!this.multicastSocket.isClosed()) {
            String message = receiveFromRelay();
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
    }


    @Override
    public void close() {
        try {
            this.multicastSocket.leaveGroup(this.multicastAddress);
        } catch (IOException e) {
            //throw new UnableToLeaveGroupException("An error has occured when trying to disconnect from the communication with the MurmurRelay.", e);
        }
    }

    @Override
    public void run() {
        executorService.scheduleAtFixedRate(this::echo, 0, 15, TimeUnit.SECONDS);
        executorService.submit(this::listen);
    }
}