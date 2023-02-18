package org.helmo.murmurG6.infrastructure.dto;

public class ServerConfigDto {
    public String serverName;
    public String base64KeyAES;

    public ServerConfigDto(String serverName, String base64KeyAES){
        this.serverName = serverName;
        this.base64KeyAES = base64KeyAES;
    }

    public ServerConfigDto(){

    }
}
