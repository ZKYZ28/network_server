package org.helmo.murmurG6.infrastructure.storage.json;

import com.google.gson.Gson;
import org.helmo.murmurG6.infrastructure.storage.json.config.JsonConfig;

public class UserJsonStorage {

    private final String directoryPath = JsonConfig.SAVE_DIR;
    private final String filePath = directoryPath.concat("\\e180280.json");
    private final Gson gson = new Gson();
}
