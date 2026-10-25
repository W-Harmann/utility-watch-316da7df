package com.utilitywatch.login;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a list in memory, so nothing is kept when the app closes. It is the default
 * storage used by {@link AuthenticationService} when no DAO is given.
 */
public class InMemoryUserDAO implements IUserDAO {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void register(User user) {
        user.setId(nextId++);
        users.add(user);
    }

    @Override
    public void update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
            }
        }
    }

    @Override
    public void delete(int userId) {
        users.removeIf(user -> user.getId() == userId);
    }

    @Override
    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}