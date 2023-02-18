package org.helmo.murmurG6.models;

public class ServerConfig {

    private final String serverName;
    private String serverIp;
    private final String base64KeyAES;

    public ServerConfig(String serverName, String base64KeyAES){
        this.serverName = serverName;
        this.base64KeyAES = base64KeyAES;
    }

    public void setServerIp(String ip){
        this.serverIp = ip;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getBase64KeyAES() {
        return base64KeyAES;
    }

    public String getServerName() {
        return serverName;
    }
}
