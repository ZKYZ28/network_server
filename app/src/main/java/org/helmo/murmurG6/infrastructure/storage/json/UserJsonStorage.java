package org.helmo.murmurG6.infrastructure.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.helmo.murmurG6.infrastructure.storage.json.config.JsonConfig;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.repository.UserCollectionRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class UserJsonStorage implements UserCollectionRepository {

    private final String directoryPath = JsonConfig.SAVE_DIR;
    private final String filePath = directoryPath.concat("/murmur_user_storage.json");
    private final Gson gson = new Gson();



    @Override
    public void save(UserCollection uc) throws SaveUserCollectionException {
        controlDirectoryExistence();

        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE)){
            gson.toJson(uc.getRegisteredUsers(), bufferedWriter);
        }catch(IOException e){
            throw new SaveUserCollectionException("Impossible de sauvegarder la liste d\'utilisateur!");
        }
    }

    @Override
    public UserCollection read() throws IOException {
        controlDirectoryExistence();
        controlFileExistence();
        ArrayList<User> result = new ArrayList<User>();

        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))){
            result = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<User>>(){}.getType());

        }catch(IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d\'utilisateur!");
        }
        return new UserCollection(result);
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
