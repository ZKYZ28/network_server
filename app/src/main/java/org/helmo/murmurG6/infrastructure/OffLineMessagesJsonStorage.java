package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.infrastructure.dto.Mapper;
import org.helmo.murmurG6.infrastructure.dto.OffLineMessageDto;
import org.helmo.murmurG6.infrastructure.dto.UserCredentialsDto;
import org.helmo.murmurG6.models.OffLineMessage;
import org.helmo.murmurG6.models.UserCredentials;
import org.helmo.murmurG6.repository.OffLineMessageRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadOffLineMessageLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveOffLineMessageLibraryException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class OffLineMessagesJsonStorage implements OffLineMessageRepository {

    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "offlineMessages.json");
    private final Gson gson = new Gson();

    @Override
    public void save(Map<String , TreeSet<OffLineMessage>> messages) throws UnableToSaveOffLineMessageLibraryException {
        createFile(FILE_PATH);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(Mapper.offLineMessagesToDto(messages), new TypeToken<Map<String, TreeSet<OffLineMessageDto>>>(){}.getType(), bufferedWriter);
        } catch (IOException e) {
            throw new UnableToSaveOffLineMessageLibraryException("Impossible de sauvegarder les messages hors-ligne!");
        }
    }


    @Override
    public Map<String, TreeSet<OffLineMessage>> load() throws UnableToLoadOffLineMessageLibraryException {
        createFile(FILE_PATH);

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            Map<String, TreeSet<OffLineMessageDto>> resultDto = gson.fromJson(reader, new TypeToken<Map<String, TreeSet<OffLineMessageDto>>>(){}.getType());
            return Mapper.offLineMessagesFromDto(resultDto);
        } catch (IOException e) {
            throw new UnableToLoadOffLineMessageLibraryException("Impossible de charger la liste de messages hors ligne!");
        }
    }


    /**
     * Crée un fichier à l'emplacement spécifié. Si le répertoire parent de l'emplacement n'existe pas, il sera créé.
     *
     * @param path l'emplacement du fichier à créer
     */
    private void createFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            //TODO Treatment
        }
    }
}
