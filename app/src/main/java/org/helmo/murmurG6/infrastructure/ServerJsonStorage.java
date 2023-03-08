package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import org.helmo.murmurG6.controller.ServerConfig;
import org.helmo.murmurG6.infrastructure.dto.ServerConfigDto;
import org.helmo.murmurG6.repository.ServerRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadServerConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerJsonStorage implements ServerRepository {

    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "/config.json");
    private final Gson gson = new Gson();


    @Override
    public ServerConfig load() throws UnableToLoadServerConfigurationException {
        ServerConfigDto serverConfigDto;
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            serverConfigDto = gson.fromJson(reader, ServerConfigDto.class);
        } catch (IOException e) {
            throw new UnableToLoadServerConfigurationException("Impossible de charger les informations de configuration du serveur !");
        }
        return new ServerConfig(serverConfigDto.serverDomain, serverConfigDto.base64KeyAES, serverConfigDto.multicastIp, serverConfigDto.mutlicastPort, serverConfigDto.serverPort, serverConfigDto.tls);
    }
}
