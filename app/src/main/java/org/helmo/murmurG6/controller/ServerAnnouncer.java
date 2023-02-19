package org.helmo.murmurG6.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerAnnouncer implements Runnable {

    private final InetAddress multicastIp;
    private final int port;
    private final ScheduledExecutorService executorService;
    private final String echoMessage;

    public ServerAnnouncer(ServerController server, String multicastIpStr, int port) {
        this.port = port;
        echoMessage = String.format("ECHO %d %s", port, server.getDomain());
        executorService = Executors.newSingleThreadScheduledExecutor();
        try {
            multicastIp = InetAddress.getByName(multicastIpStr);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void echo() {
        byte[] buf = echoMessage.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, multicastIp, port);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error sending ECHO message: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        executorService.scheduleAtFixedRate(this::echo, 0, 15, TimeUnit.SECONDS);
    }
}
