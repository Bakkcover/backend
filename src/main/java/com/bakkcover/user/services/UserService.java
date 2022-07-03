package com.bakkcover.user.services;

import org.springframework.security.core.Authentication;

public interface UserService {
    String getCognitoSub(Authentication authentication);
}
