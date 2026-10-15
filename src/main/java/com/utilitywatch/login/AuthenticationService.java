package com.utilitywatch.login;

import com.utilitywatch.PasswordHasher;

import java.util.Objects;

public class AuthenticationService {

    private final IUserDAO userDAO;

    // defaults to in-memory storage so this works out of the box without a real DB
    public AuthenticationService() {
        this(new InMemoryUserDAO());
    }

    public AuthenticationService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    /** @throws IllegalArgumentException if the email is invalid or already taken */
    public User signUp(String email, String password) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = new User(email, password);
        userDAO.register(user);
        return user;
    }

    /** @throws IllegalArgumentException if the email or password is incorrect */
    public User logIn(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user == null || !user.getPassword().equals(PasswordHasher.hash(password))) {
            throw new IllegalArgumentException("Incorrect email or password");
        }
        Session.getInstance().login(user);
        return user;
    }

    public boolean logOut(User user) {
        Session.getInstance().logout();
        return true;
    }

    /**
     * Updates name, email and property address together (US13.4). Name and email are required;
     * the property address may be left blank. Everything is checked before anything is changed,
     * so a refused update leaves the previous values in place.
     * @throws IllegalArgumentException if name or email is blank, the email is invalid, or the email belongs to another account
     */
    public void updateProfile(User user, String name, String email, String propertyAddress) {
        String newName = name == null ? "" : name.trim();
        String newEmail = email == null ? "" : email.trim();
        String newAddress = propertyAddress == null ? "" : propertyAddress.trim();

        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (newEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!newEmail.equals(user.getEmail()) && userDAO.findByEmail(newEmail) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String oldName = user.getName();
        String oldEmail = user.getEmail();
        String oldAddress = user.getPropertyAddress();

        user.setName(newName);
        user.setEmail(newEmail);
        user.setPropertyAddress(newAddress.isEmpty() ? null : newAddress);
        try {
            userDAO.update(user);
        } catch (RuntimeException e) {
            // couldn't save, so put the previous values back
            user.setName(oldName);
            user.setEmail(oldEmail);
            user.setPropertyAddress(oldAddress);
            throw e;
        }
    }

    /** Permanently removes the account and ends the session. (The user's readings are removed separately.) */
    public void deleteAccount(User user) {
        userDAO.delete(user.getId());
        Session.getInstance().logout();
    }

    /** @throws IllegalArgumentException if oldEmail is wrong, newEmail is invalid, or newEmail is taken */
    public void updateEmail(User user, String oldEmail, String newEmail) {
        if (!user.getEmail().equals(oldEmail)) {
            throw new IllegalArgumentException("Incorrect current email");
        }
        if (newEmail.isEmpty()) {
            return; // empty means "keep what's there"
        }
        if (newEmail.equals(oldEmail)) {
            throw new IllegalArgumentException("New email must be different");
        }
        if (!isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (userDAO.findByEmail(newEmail) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setEmail(newEmail);
    }

    /** @throws IllegalArgumentException if oldPassword is wrong or newPassword matches it */
    public void updatePassword(User user, String oldPassword, String newPassword) {
        if (!user.getPassword().equals(PasswordHasher.hash(oldPassword))) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        if (newPassword.isEmpty()) {
            return;
        }
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("New password must be different");
        }
        user.setPassword(PasswordHasher.hash(newPassword));
    }

    /** @throws IllegalArgumentException if oldAddress is wrong or newAddress matches it */
    public void updatePropertyAddress(User user, String oldAddress, String newAddress) {
        if (!Objects.equals(user.getPropertyAddress(), oldAddress)) {
            throw new IllegalArgumentException("Incorrect current property address");
        }
        if (newAddress.isEmpty()) {
            return;
        }
        if (newAddress.equals(oldAddress)) {
            throw new IllegalArgumentException("New property address must be different");
        }
        user.setPropertyAddress(newAddress);
    }
}