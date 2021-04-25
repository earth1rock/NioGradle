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


    private final String nameOfRoom;
    private final Set<Room> rooms;

    public CommandJoin(String nameOfRoom, Set<Room> rooms) {
        this.nameOfRoom = Objects.requireNonNull(nameOfRoom, "Commands are null");
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
