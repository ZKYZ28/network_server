package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.models.TrendLibrary;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TrendJsonStorage implements TrendRepository {

    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "trends.json");
    private final Gson gson = new Gson();


    public void save(TrendLibrary library) throws SaveUserCollectionException {
        createFile(FILE_PATH);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(library, new TypeToken<TrendLibrary>() {
            }.getType(), bufferedWriter);
        } catch (IOException e) {
            throw new SaveUserCollectionException("Impossible de sauvegarder la liste d'utilisateur!");
        }
    }


    public TrendLibrary load() throws ReadUserCollectionException {
        createFile(FILE_PATH);

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, new TypeToken<TrendLibrary>() {
            }.getType());
        } catch (IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d'utilisateurs!");
        }
    }

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
