package server.commands;

import client.Validator;
import message.Message;
import message.MessageType;
import room.Room;
import session.Session;

import java.util.Objects;
import java.util.Set;

public class CommandCreateRoom implements Command {


    private final String nameOfRoom;
    private final Set<Room> rooms;

    public CommandCreateRoom(String nameOfRoom, Set<Room> rooms) {
        this.nameOfRoom = Objects.requireNonNull(nameOfRoom, "Commands are null");
        this.rooms = Objects.requireNonNull(rooms, "Set of rooms is null");
    }

    @Override
    public void execute(Session session) throws Exception {

        Room room = new Room(nameOfRoom);
        if (!rooms.add(room)) {
            Message roomCreateError = new Message(MessageType.MESSAGE, "[SERVER]", nameOfRoom + " this room already exist");
            session.writeMessage(roomCreateError);
        } else {
            Message roomCreateMessage = new Message(MessageType.MESSAGE, "[SERVER]", nameOfRoom + " room was created");
            session.writeMessage(roomCreateMessage);
        }
    }
}
