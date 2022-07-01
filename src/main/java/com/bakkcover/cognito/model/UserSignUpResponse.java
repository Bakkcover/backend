package com.bakkcover.cognito.model;

public class UserSignUpResponse {
    private String email;
    private String username;

    private String errorMessage;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername(String username) {
        return this.username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
