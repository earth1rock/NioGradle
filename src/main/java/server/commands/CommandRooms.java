package server.commands;

import message.Message;
import message.MessageType;
import room.Room;
import session.Session;

import java.util.Set;

public class CommandRooms implements Command {
    private final Set<Room> rooms;

    public CommandRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    private String getRooms() {
        StringBuilder result = new StringBuilder();
        for (Room room : rooms) {
            result.append(room.toString()).append("\n");
        }
        return result.substring(0, result.length() - 1);
    }

    @Override
    public void execute(Session session) throws Exception {
        Message roomsList = new Message(MessageType.MESSAGE, "[SERVER]", getRooms());
        session.writeMessage(roomsList);
    }
}
