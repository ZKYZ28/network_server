package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.infrastructure.dto.Mapper;
import org.helmo.murmurG6.infrastructure.dto.TrendLibraryDto;
import org.helmo.murmurG6.models.TrendLibrary;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;

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


    public void save(TrendLibrary library) throws UnableToSaveTrendLibraryException {
        createFile(FILE_PATH);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(Mapper.toDto(library), new TypeToken<TrendLibraryDto>() {
            }.getType(), bufferedWriter);
        } catch (IOException e) {
            throw new UnableToSaveTrendLibraryException("Impossible de sauvegarder la liste d'utilisateur!");
        }
    }


    public TrendLibrary load() throws UnableToLoadTrendLibraryException {
        createFile(FILE_PATH);

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            return Mapper.fromDto(gson.fromJson(reader, new TypeToken<TrendLibrary>() {}.getType()));

        } catch (IOException e) {
            throw new UnableToLoadTrendLibraryException("Impossible de charger la liste d'utilisateurs!");
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
