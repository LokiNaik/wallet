package com.wallet.dto;


public class CreateUserRequest {

    private String name;

    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
