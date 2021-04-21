package server.commands;

import client.User;
import client.Validator;
import message.Message;
import message.MessageType;
import room.Room;
import session.Session;

import java.util.Objects;
import java.util.Set;

public class CommandJoin implements Command {

    private final Validator validator;
    private final String[] commands;
    private final Set<Room> rooms;

    public CommandJoin(Validator validator, String[] commands, Set<Room> rooms) {
        this.validator = Objects.requireNonNull(validator, "Validator is null");
        this.commands = Objects.requireNonNull(commands, "Commands are null");
        this.rooms = Objects.requireNonNull(rooms, "Set of rooms is null");
    }

    private Room roomIsExist(String nameOfRoom) {
        for (Room room : rooms) {
            if (room.getName().equals(nameOfRoom)) {
                return room;
            }
        }
        return null;
    }

    @Override
    public void execute(Session session) throws Exception {
        if (!validator.validateCommand(commands)) {
            Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", "Failed to join to room! Type '/help' for more information");
            session.writeMessage(errorMessage);
            return;
        }
        String nameOfRoom = commands[1];
        Room targetRoom = roomIsExist(nameOfRoom);
        if (targetRoom == null) {
            Message roomNotExist = new Message(MessageType.MESSAGE, "[SERVER]", nameOfRoom + " room is not exist");
            session.writeMessage(roomNotExist);
            return;
        }
        Room currentRoom = session.getRoom();
        User currentUser = session.getUser();

        currentRoom.removeUser(currentUser);
        currentUser.setRoom(targetRoom);
        targetRoom.addUser(currentUser);
        Message joinToRoom = new Message(MessageType.MESSAGE, "[SERVER]", "You joined to " + nameOfRoom + " room");
        session.writeMessage(joinToRoom);
    }
}
