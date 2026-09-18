package com.utilitywatch.login;

import com.utilitywatch.PasswordHasher;

// one registered account - email, hashed password, property address, plus id once saved
public class User {

    private int id;
    private String email;
    private String passwordHash;
    private String propertyAddress;

    public User(String email, String password) {
        this.email = email;
        this.passwordHash = PasswordHasher.hash(password);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    // package-private on purpose - changes should go through AuthenticationService's
    // validation, not be set directly from outside this package
    void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return passwordHash;
    }

    void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }
}