package com.utilitywatch.login;

import com.utilitywatch.PasswordHasher;

/**
 * One registered account: name, email, hashed password and property address, plus an id once saved.
 * <p>The setters for name, email and password hash are package-private on purpose, so changes go
 * through {@link AuthenticationService}, which validates them.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String propertyAddress;

    /**
     * Creates a user. The password is hashed with {@link com.utilitywatch.PasswordHasher}, so the plain
     * text is not kept.
     *
     * @param email the user's email address
     * @param password the plain-text password
     */
    public User(String email, String password) {
        this.email = email;
        this.passwordHash = PasswordHasher.hash(password);
    }

    /**
     * Returns the database id.
     *
     * @return the id given when the user was saved, or 0 if not saved yet
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database id. The DAO calls this after saving a user and when it loads one.
     *
     * @param id the database id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's name.
     *
     * @return the name, or {@code null} if none has been entered
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name. Package-private on purpose, same as email: changes go through
     * {@link AuthenticationService}.
     *
     * @param name the new name
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email. Package-private on purpose: changes should go through
     * {@link AuthenticationService}'s validation, not be set directly from outside this package.
     *
     * @param email the new email
     */
    void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the stored password hash (never the plain text).
     *
     * @return the password hash
     */
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Sets the stored password hash. The value is stored as given, not hashed again.
     *
     * @param passwordHash the new password hash
     */
    void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the property address.
     *
     * @return the address, or {@code null} if none has been entered
     */
    public String getPropertyAddress() {
        return propertyAddress;
    }

    /**
     * Sets the property address.
     *
     * @param propertyAddress the new address, or {@code null} for none
     */
    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }
}