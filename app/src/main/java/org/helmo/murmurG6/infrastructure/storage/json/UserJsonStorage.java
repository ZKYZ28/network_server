package org.helmo.murmurG6.infrastructure.storage.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.infrastructure.dto.UserDto;
import org.helmo.murmurG6.infrastructure.storage.json.config.JsonConfig;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.repository.IUserCollectionRepository;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class UserJsonStorage implements IUserCollectionRepository {
    private final Path FILE_PATH = Paths.get(JsonConfig.SAVE_DIR, "/user.json");
    private final Gson gson = new Gson();

    @Override
    public void save(Iterable<User> uc) throws SaveUserCollectionException {
        createFile(FILE_PATH);

        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE)){
            gson.toJson(uc, bufferedWriter);
        }catch(IOException e){
            throw new SaveUserCollectionException("Impossible de sauvegarder la liste d'utilisateur!");
        }
    }

    @Override
    public List<User> read() throws IOException {
        createFile(FILE_PATH);
        List<UserDto> resultDto;
        List<User> resultUser= new ArrayList<>();

        try(BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)){
            resultDto = this.gson.fromJson(reader, new TypeToken<ArrayList<UserDto>>(){}.getType());

            if (resultDto != null) {
                for(UserDto dto : resultDto){
                    resultUser.add(UserDto.userDtoToUser(dto));
                }
            }
            return Objects.requireNonNullElseGet(resultUser, ArrayList::new); //Retourne le result si non null, sinon, retourne une nouvelle ArrayList
        }catch(IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d'utilisateur!");
        }
    }


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