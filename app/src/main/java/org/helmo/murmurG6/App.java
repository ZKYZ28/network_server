package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.storage.json.UserJsonStorage;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

import java.util.ArrayList;
import java.util.List;

public class App {

    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();





    public static void main(String[] args) throws SaveUserCollectionException {
        /*try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        User u = new User("antho", "azerty", 14, "femme");

        UserCollection uc = new UserCollection();
        UserJsonStorage uj = new UserJsonStorage();



        List<User> ul = new ArrayList<>();
        ul.add(u);
        uc.setRegisteredUsers(ul);

        uj.save(uc.getRegisteredUsers());
    }
}
