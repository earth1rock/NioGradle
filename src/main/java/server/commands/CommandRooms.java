package server.commands;

import message.Message;
import message.MessageType;
import room.Room;
import session.Session;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandRooms implements Command {
    private final Set<Room> rooms;

    public CommandRooms(Set<Room> rooms) {
        this.rooms = Objects.requireNonNull(rooms, "Set of rooms is null");
    }

    private String getRooms() {
        return "List of rooms:\n" + rooms.stream().map(Room::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public void execute(Session session) throws IOException {
        Message roomsList = new Message(MessageType.MESSAGE, "[SERVER]", getRooms());
        session.writeMessage(roomsList);
    }
}
