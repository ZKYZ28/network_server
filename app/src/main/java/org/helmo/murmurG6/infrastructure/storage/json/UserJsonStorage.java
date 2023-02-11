package org.helmo.murmurG6.infrastructure.storage.json;

import com.google.gson.Gson;
import org.helmo.murmurG6.infrastructure.storage.json.config.JsonConfig;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.repository.IUserCollectionRepository;
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
    private final String filePath = "app/src/main/java/org/helmo/murmurG6/Resources/jsonFilesStorage/jsonStorageFile.json";
            //directoryPath.concat("/murmur_user_storage.json");



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
        /*controlDirectoryExistence();
        controlFileExistence();
        ArrayList<User> result = new ArrayList<User>();

        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))){
            result = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<User>>(){}.getType());

        }catch(IOException e) {
            throw new ReadUserCollectionException("Impossible de charger la liste d\'utilisateur!");
        }*/
        return new ArrayList<User>();
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
