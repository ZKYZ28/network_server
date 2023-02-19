package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.AESCrypt;
import org.helmo.murmurG6.models.Task;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class RelayThread implements Runnable, AutoCloseable {

    private static final String MULTICAST_IP = "224.1.1.255";
    private static final int PORT = 19375;
    private static final String NETWORK_INTERFACE = "eth12";
    private static RelayThread instance;


    private final ScheduledExecutorService executorService;
    private InetAddress multicastAddress;
    private InetSocketAddress group;
    private MulticastSocket multicastSocket;
    private ServerController server;


    private RelayThread() {
        executorService = Executors.newScheduledThreadPool(2);

        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);

            this.multicastAddress = InetAddress.getByName(MULTICAST_IP);
            this.multicastSocket = new MulticastSocket(PORT);
            this.group = new InetSocketAddress(multicastAddress, PORT);

            this.multicastSocket.joinGroup(group, networkInterface);
        } catch (IOException e) {
            //throw new UnableToCreateRelayThreadException("An error has occured when trying to initialize the connection with the MurmurRelay.", e);
        }
    }

    public static RelayThread getInstance() {
        if (instance == null) {
            instance = new RelayThread();
        }
        return instance;
    }

    public void init(ServerController server) {
        this.server = server;
    }


    public void sendToRelay(String sendMessage) {
        try {
            byte[] msgsBytes = AESCrypt.encrypt(sendMessage, server.getServerConfig().getBase64KeyAES());
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
            return AESCrypt.decrypt(packet.getData(), server.getServerConfig().getBase64KeyAES());
        } catch (Exception e) {
            return null;
        }
    }

    public void echo() {
        try {
            String echoMessage = "ECHO " + PORT + " " + server.getDomain();
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
                //1. Retrieve the ClientRunnable
                ClientRunnable receiver = server.getClientRunnableByLogin("login");

                if (receiver != null) {

                    //2. Retrieve the TaskType
                    Task task = Protocol.buildTask(args.group("content"));

                    //3. Attach the client to the task
                    task.setClient(receiver);

                    //4. Give the task to the executor
                    executor.addTask(task);
                }
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