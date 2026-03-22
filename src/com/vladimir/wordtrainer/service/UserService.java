package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.db.UserRepository;
import com.vladimir.wordtrainer.db.UserDictionaryRepository;

public class UserService {
    private final UserRepository userRepository;
    private final UserDictionaryRepository userDictionaryRepository;

    public UserService(UserRepository userRepository, UserDictionaryRepository userDictionaryRepository) {
        this.userRepository = userRepository;
        this.userDictionaryRepository = userDictionaryRepository;
    }

    public boolean isUserExists(long telegramUserId) {
        return userRepository.isUserExists(telegramUserId);
    }

    public void register(long telegramUserId, String username, String firstName, String lastName) {
        userRepository.saveUser(telegramUserId, username, firstName, lastName);
        userDictionaryRepository.setAllPublicDictionariesToUser(telegramUserId);
    }
}
