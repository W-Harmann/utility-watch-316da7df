package com.utilitywatch.login;

import java.util.ArrayList;
import java.util.List;

// just a list in memory - good enough until real persistence is wired in
public class InMemoryUserDAO implements IUserDAO {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void register(User user) {
        user.setId(nextId++);
        users.add(user);
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