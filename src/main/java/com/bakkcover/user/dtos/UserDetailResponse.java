package com.bakkcover.user.dtos;

import com.bakkcover.user.entities.User;

public class UserDetailResponse {
    private User user;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
