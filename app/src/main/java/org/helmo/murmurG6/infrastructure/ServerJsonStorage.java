package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.helmo.murmurG6.repository.ServerRepository;
import org.helmo.murmurG6.repository.exceptions.ReadServerConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerJsonStorage implements ServerRepository {

    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "/serverConfig.json");

    private final Gson gson = new Gson();



    @Override
    public String loadKeyAes() throws ReadServerConfigurationException {
        StringBuilder jsonString = new StringBuilder(); // The JSON string read from the file

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            throw new ReadServerConfigurationException("Impossible de charger les informations de configuration du serveur !");
        }
        JsonObject jsonObject = gson.fromJson(jsonString.toString(), JsonObject.class);
        return jsonObject.get("base64KeyAES").getAsString();
    }

}
