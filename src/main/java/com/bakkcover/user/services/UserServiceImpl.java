package com.bakkcover.user.services;

import com.bakkcover.user.entities.User;
import com.bakkcover.user.exceptions.CustomException;
import com.bakkcover.user.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getCognitoSub(Authentication authentication) {
        return authentication.getName();
    }

    @Override
    public User getUser(Authentication authentication) throws CustomException {
        String id = authentication.getName();
        Optional<User> result = this.userRepository.findById(id);

        if (result.isEmpty()) {
            throw new CustomException("No user found!");
        }

        return result.get();
    }
}
