package room;

import client.User;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Room {

    private final String name;
    private final Set<User> users = new HashSet<>();

    public Room(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", users=" + users.size() +
                '}';
    }
}
