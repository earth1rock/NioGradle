package server.commands;

import client.Validator;
import message.Message;
import message.MessageType;
import room.Room;
import session.Session;

import java.util.Set;

public class CommandCreateRoom implements Command{

    private final Validator validator;
    private final String[] commands;
    private final Set<Room> rooms;

    public CommandCreateRoom(Validator validator, String[] commands, Set<Room> rooms) {
        this.validator = validator;
        this.commands = commands;
        this.rooms = rooms;
    }

    @Override
    public void execute(Session session) throws Exception {
        if (!validator.validateCommand(commands)) {
            Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", "Failed to create new room! Type '/help' for more information");
            session.writeMessage(errorMessage);
            return;
        }
        String nameOfRoom = commands[1];
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
