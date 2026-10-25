package com.utilitywatch.login;

import com.utilitywatch.PasswordHasher;

import java.util.Objects;

/**
 * Handles registration, login, logout, profile updates and account deletion. It works through an
 * {@link IUserDAO}, so it does not depend on how users are stored.
 */
public class AuthenticationService {

    private final IUserDAO userDAO;

    /** Creates a service that stores users in memory, so it works out of the box without a database. */
    public AuthenticationService() {
        this(new InMemoryUserDAO());
    }

    /**
     * Creates a service that stores users through the given DAO.
     *
     * @param userDAO where users are stored
     */
    public AuthenticationService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    /**
     * Registers a new user.
     *
     * @param email the new account's email address
     * @param password the plain-text password
     * @return the registered user, with its id set
     * @throws IllegalArgumentException if the email is invalid or already taken
     */
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

    /**
     * Logs a user in and records them in the {@link Session}.
     *
     * @param email the account's email address
     * @param password the plain-text password
     * @return the logged-in user
     * @throws IllegalArgumentException if the email or password is incorrect
     */
    public User logIn(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user == null || !user.getPassword().equals(PasswordHasher.hash(password))) {
            throw new IllegalArgumentException("Incorrect email or password");
        }
        Session.getInstance().login(user);
        return user;
    }

    /**
     * Logs out by clearing the {@link Session}.
     *
     * @param user the user logging out (not used; the session is cleared either way)
     * @return always {@code true}
     */
    public boolean logOut(User user) {
        Session.getInstance().logout();
        return true;
    }

    /**
     * Updates name, email and property address together (US13.4). Name and email are required;
     * the property address may be left blank. Everything is checked before anything is changed,
     * so a refused update leaves the previous values in place. The new details are saved through
     * the DAO.
     *
     * @param user the user to update
     * @param name the new name; leading and trailing spaces are removed
     * @param email the new email; leading and trailing spaces are removed
     * @param propertyAddress the new address; a blank value clears it
     * @throws IllegalArgumentException if name or email is blank, the email is invalid, or the email belongs to another account
     * @throws RuntimeException if the details cannot be saved; the previous values are put back first
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

    /**
     * Permanently removes the account and ends the session. The user's readings are removed
     * separately, through {@link com.utilitywatch.database.IReadingDAO#deleteReadingsForUser(int)}.
     *
     * @param user the user whose account is deleted
     */
    public void deleteAccount(User user) {
        userDAO.delete(user.getId());
        Session.getInstance().logout();
    }

    /**
     * Changes the email of a {@link User} object. If {@code newEmail} is empty, nothing changes.
     * This changes the object only; it does not save the change through the DAO (use
     * {@link #updateProfile(User, String, String, String)} for that).
     *
     * @param user the user to change
     * @param oldEmail the user's current email, which must match
     * @param newEmail the new email
     * @throws IllegalArgumentException if oldEmail is wrong, newEmail is invalid, or newEmail is taken
     */
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

    /**
     * Changes the password of a {@link User} object. If {@code newPassword} is empty, nothing changes.
     * This changes the object only; it does not save the change through the DAO.
     *
     * @param user the user to change
     * @param oldPassword the user's current plain-text password, which must match
     * @param newPassword the new plain-text password
     * @throws IllegalArgumentException if oldPassword is wrong or newPassword matches it
     */
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

    /**
     * Changes the property address of a {@link User} object. If {@code newAddress} is empty, nothing
     * changes. This changes the object only; it does not save the change through the DAO (use
     * {@link #updateProfile(User, String, String, String)} for that).
     *
     * @param user the user to change
     * @param oldAddress the user's current address, which must match
     * @param newAddress the new address
     * @throws IllegalArgumentException if oldAddress is wrong or newAddress matches it
     */
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