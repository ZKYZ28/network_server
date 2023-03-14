package org.helmo.murmurG6.controller;

import java.net.NetworkInterface;

public class ServerConfig {

    public final String serverDomain;
    public final String base64KeyAES;
    public final String multicastIp;
    public final int multicastPort;
    public final int serverPort;
    public final boolean tls;
    public NetworkInterface networkInterface;

    public ServerConfig(String serverDomain, String base64KeyAES, String multicastIp, int multicastPort, int serverPort, boolean tls) {
        this.serverDomain = serverDomain;
        this.base64KeyAES = base64KeyAES;
        this.multicastIp = multicastIp;
        this.multicastPort = multicastPort;
        this.serverPort = serverPort;
        this.tls = tls;
    }
}
