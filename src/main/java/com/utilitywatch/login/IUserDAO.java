package com.utilitywatch.login;

// keeps AuthenticationService independent of how users are actually stored
public interface IUserDAO {
    void register(User user);
    User findByEmail(String email);
    void update(User user);     // saves the user's current name, email, password and address (matched by id)
    void delete(int userId);    // removes the account permanently
}