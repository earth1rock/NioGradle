package client;

import java.util.Objects;

public class User {

    private final String name;

    public User(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }
}