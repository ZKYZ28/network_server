package org.helmo.murmurG6.models;

import org.helmo.murmurG6.repository.OffLineMessageRepository;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.*;

public class DataManager {

    private final UserRepository userRepository;
    private final TrendRepository trendRepository;
    private final OffLineMessageRepository offLineMessageRepository;

    private UserLibrary userLibrary;
    private TrendLibrary trendLibrary;
    private OfflineMessagesLibrary offlineMessagesLibrary;

    public DataManager(UserRepository userRepository, TrendRepository trendRepository, OffLineMessageRepository offLineMessageRepository){
        this.userRepository = userRepository;
        this.trendRepository = trendRepository;
        this.offLineMessageRepository = offLineMessageRepository;

        try {
            this.userLibrary = userRepository.load();
            this.trendLibrary = trendRepository.load();
            this.offlineMessagesLibrary = offLineMessageRepository.load();
        }catch (UnableToLoadUserLibraryException | UnableToLoadTrendLibraryException | UnableToLoadOffLineMessageLibraryException e){
            System.out.println(e.getMessage());
        }
    }


    public UserLibrary getUserLibrary() {
        return userLibrary;
    }

    public TrendLibrary getTrendLibrary() {
        return trendLibrary;
    }

    public OfflineMessagesLibrary getOfflineMessagesLibrary() {
        return offlineMessagesLibrary;
    }



    public void saveUsers() throws UnableToSaveUserLibraryException {
        userRepository.save(userLibrary);
    }

    public void saveTrends() throws UnableToSaveUserLibraryException, UnableToSaveTrendLibraryException {
        trendRepository.save(trendLibrary);
    }

    public void saveOfflineMessages() throws UnableToSaveOffLineMessageLibraryException {
        offLineMessageRepository.save(offlineMessagesLibrary);
    }
}
