package com.bakkcover.user.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public String getCognitoSub(Authentication authentication) {
        return authentication.getName();
    }
}
