package room;

import client.User;
import java.util.HashSet;
import java.util.Set;

public class Room {

    private final String name;
    private final Set<User> users;

    public Room(String name) {
        this.name = name;
        this.users = new HashSet<>();
    }

    public Set<User> getUsers() {
        return users;
    }

    public String getName() {
        return name;
    }

    public Room addUser(User user) {
        users.add(user);
        return this;
    }

    public Room removeUser(User user) {
        users.remove(user);
        return this;
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", users=" + users.size() +
                '}';
    }
}
