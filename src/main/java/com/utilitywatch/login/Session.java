package com.utilitywatch.login;

/**
 * Holds the user who is currently logged in. There is one shared instance, so any screen can check
 * who is logged in.
 */
public class Session {

    private static Session instance = null;
    private User currentUser;

    private Session() {
    }

    /**
     * Returns the shared session.
     *
     * @return the one shared session
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Records who is logged in.
     *
     * @param user the user who has logged in
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /** Forgets the logged-in user. */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Returns the logged-in user.
     *
     * @return the current user, or {@code null} if nobody is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Says whether anybody is logged in.
     *
     * @return {@code true} if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}