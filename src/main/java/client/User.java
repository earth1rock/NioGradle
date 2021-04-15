package client;

import room.Room;

public class User {

    private final String name;
    private final static Room DEFAULT_ROOM = new Room("DEFAULT");
    private Room room;

    public User(String name) {
        this.name = name;
        this.room = DEFAULT_ROOM;
        DEFAULT_ROOM.addUser(this);
    }

    public String getName() {
        return name;
    }

    public Room getRoom() {
        return room;
    }

    public static Room getDefaultRoom() {
        return DEFAULT_ROOM;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}