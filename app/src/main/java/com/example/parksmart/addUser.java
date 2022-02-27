package com.example.parksmart;

public class addUser {
    private String name, email, password;

    public addUser(){
        //public no-arg constructor needed
    }

    public addUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
