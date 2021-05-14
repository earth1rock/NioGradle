package client;

import java.util.Objects;

public class User {

    private final String name;

    public User(String name) {
        Objects.requireNonNull(name, "Username cannot be null");
        if (name.trim().equals("")) throw new IllegalArgumentException("Incorrect name format");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}