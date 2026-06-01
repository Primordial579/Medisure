package com.medisure.auth.dto;

public class AuthResponse {

    private String token;
    private String username;
    private String userType;
    private String linkedUserId;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String userType, String linkedUserId, String message) {
        this.token = token;
        this.username = username;
        this.userType = userType;
        this.linkedUserId = linkedUserId;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
