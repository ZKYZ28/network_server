package org.helmo.murmurG6.infrastructure.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class UserJsonStorage implements IUserCollectionRepository {

    private final String directoryPath = JsonConfig.SAVE_DIR;
    private final String filePath = directoryPath.concat("/user.json");
    Gson gson = new Gson();



    @Override
    public void save(Iterable<User> uc) throws SaveUserCollectionException {
        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE)){
            gson.toJson(uc, bufferedWriter);
        }catch(IOException e){
            throw new SaveUserCollectionException("Impossible de sauvegarder la liste d\'utilisateur!");
        }
    }

    @Override
    public List<User> read() throws IOException {
        controlDirectoryExistence();
        controlFileExistence();

        List<UserDto> resultDto = new ArrayList<>();
        List<User> resultUser= new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))){
            resultDto = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<UserDto>>(){}.getType());

            for(UserDto dto : resultDto){
                resultUser.add(UserDto.userDtoToUser(dto));
            }
        }catch(IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d\'utilisateur!");
        }
        return resultUser;
    }



    private void controlDirectoryExistence () {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }


    private void controlFileExistence() throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            f.createNewFile();
        }
    }

}
