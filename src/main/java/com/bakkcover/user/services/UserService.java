package com.bakkcover.user.services;

import com.bakkcover.user.entities.User;
import com.bakkcover.user.exceptions.CustomException;
import org.springframework.security.core.Authentication;

public interface UserService {
    String getCognitoSub(Authentication authentication);

    User getUser(Authentication authentication) throws CustomException;
}
