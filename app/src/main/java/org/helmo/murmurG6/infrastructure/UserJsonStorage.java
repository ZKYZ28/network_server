package org.helmo.murmurG6.infrastructure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.infrastructure.dto.Mapper;
import org.helmo.murmurG6.infrastructure.dto.UserDto;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Cette classe implémente l'interface {@link UserRepository} et permet de sauvegarder et de lire des collections d'utilisateurs sous forme de fichier JSON.
 * La classe utilise la bibliothèque Google Gson pour effectuer les opérations de conversion entre les objets Java et les données JSON.
 * Les fichiers sont sauvegardés dans un emplacement spécifié par la classe {@link JsonConfig}.
 *
 * @version 1.0
 * @since 11 février 2023
 */
public class UserJsonStorage implements UserRepository {
    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "/user.json");
    private final Gson gson = new Gson();


    @Override
    public void save(UserLibrary uc) throws SaveUserCollectionException {
        createFile(FILE_PATH);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            gson.toJson(Mapper.userDtoListFromUsers(new ArrayList<>(uc.values())), new TypeToken<ArrayList<UserDto>>() {
            }.getType(), bufferedWriter);
        } catch (IOException e) {
            throw new SaveUserCollectionException("Impossible de sauvegarder la liste d'utilisateur!");
        }
    }

    /**
     * {@inheritDoc}
     * Cette méthode lit une collection d'utilisateurs enregistrée en tant que fichier JSON à l'emplacement spécifié.
     * Si le fichier n'existe pas, il sera créé.
     */
    @Override
    public UserLibrary load() throws ReadUserCollectionException {
        createFile(FILE_PATH);

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            Iterable<UserDto> resultDto = gson.fromJson(reader, new TypeToken<ArrayList<UserDto>>() {
            }.getType());
            return UserLibrary.of(Mapper.userListFromDto(resultDto));
        } catch (IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d'utilisateurs!");
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
            e.printStackTrace();
        }
    }
}