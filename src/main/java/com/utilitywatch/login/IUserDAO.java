package com.utilitywatch.login;

// keeps AuthenticationService independent of how users are actually stored
public interface IUserDAO {
    void register(User user);
    User findByEmail(String email);
}