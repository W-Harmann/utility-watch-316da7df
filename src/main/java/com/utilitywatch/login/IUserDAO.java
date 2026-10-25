package com.utilitywatch.login;

/**
 * Data access object for user accounts. It keeps {@link AuthenticationService} independent of how
 * users are actually stored.
 */
public interface IUserDAO {

    /**
     * Saves a new user and sets the generated id on it.
     *
     * @param user the user to save
     */
    void register(User user);

    /**
     * Finds a user by email address.
     *
     * @param email the email to look for
     * @return the user with that email, or {@code null} if there is none
     */
    User findByEmail(String email);

    /**
     * Saves the user's current name, email, password hash and property address, matched by id.
     *
     * @param user the user with its new details
     */
    void update(User user);

    /**
     * Removes an account permanently. Does nothing if it does not exist.
     *
     * @param userId the id of the account to remove
     */
    void delete(int userId);
}