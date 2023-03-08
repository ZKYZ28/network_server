package org.helmo.murmurG6.infrastructure.dto;

public class ServerConfigDto {
    public String serverDomain;
    public String base64KeyAES;
    public String multicastIp;
    public int mutlicastPort;
    public int serverPort;
    public boolean tls;


    public ServerConfigDto(String serverName, String base64KeyAES, String multicastIp, int multicastPort, int serverPort, boolean tls){
        this.serverDomain = serverName;
        this.base64KeyAES = base64KeyAES;
        this.multicastIp = multicastIp;
        this.mutlicastPort = multicastPort;
        this.serverPort = serverPort;
        this.tls = tls;
    }

    public ServerConfigDto() { }
}
