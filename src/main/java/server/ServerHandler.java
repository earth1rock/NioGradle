package server;

import client.User;
import client.Validator;
import codec.Session;
import message.Message;
import message.MessageFormatter;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import room.Room;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ServerHandler {
    private final Selector selector;
    private final Viewer viewer;
    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final static String helpMessageString = "\nType '/rooms' for room list\n" +
            "Type '/createroom [name_of_room]' for create new room. [name_of_room] length must be in [7; 127]\n" +
            "Type '/join [name_of_room]' to join to room";
    private final Set<Room> rooms;
    private final Validator validator;

    public ServerHandler(Selector selector, Viewer viewer, Set<Room> rooms) {
        this.selector = selector;
        this.viewer = viewer;
        this.rooms = rooms;
        validator = new Validator(viewer);
    }

    private String getRooms() {
        StringBuilder result = new StringBuilder();
        for (Room room : rooms) {
            result.append(room.toString()).append("\n");
        }
        return result.substring(0, result.length() - 1);
    }

    public void doTask(Session session, Message message) throws Exception {

        switch (message.getMessageType()) {

            case MessageType.MESSAGE:
                Room room = session.getRoom();
                echo(message, room);
                viewer.print(message);
                break;
            case MessageType.JOIN:
                User newUser = new User(message.getUserName());
                session.attachUser(newUser);
                Message joinMessage = new Message(MessageType.MESSAGE, "[SERVER]", message.getUserName() + " connected to the server");
                echo(joinMessage);
                String formattedMessage = MessageFormatter.formatForLogs(joinMessage);
                logger.info(formattedMessage);
                break;
            case MessageType.LEAVE:
                Message leaveMessage = new Message(MessageType.LEAVE, "[SERVER]", message.getUserName() + " disconnected from the server");
                session.writeMessage(leaveMessage);
                User leaveUser = session.getUser();
                session.getRoom().removeUser(leaveUser);
                session.close();
                leaveMessage.setMessageType(MessageType.MESSAGE);
                echo(leaveMessage);
                formattedMessage = MessageFormatter.formatForLogs(leaveMessage);
                logger.info(formattedMessage);
                break;
            case MessageType.COMMAND:
                executeCommand(message, session);
                break;
        }
    }

    private void executeCommand(Message message, Session mainSession) {

        String editedMsg = message.getMessage().trim().replaceAll("\\s+", " ");
        String[] commands = editedMsg.split(" ", 2);
        String command = commands[0];

        try {
            switch (command) {
                case "/list":
                    for (SelectionKey key : selector.keys()) {
                        if (key.isValid() && key.channel() instanceof SocketChannel) {
                            Session session = (Session) key.attachment();
                            Message tempMessage = new Message(MessageType.MESSAGE, "[SERVER]", session.getUser().getName());
                            mainSession.writeMessage(tempMessage);
                        }
                    }
                    break;

                case "/help":
                    Message helpMessage = new Message(MessageType.MESSAGE, "[SERVER]", helpMessageString);
                    mainSession.writeMessage(helpMessage);
                    break;

                case "/rooms":
                    Message roomsList = new Message(MessageType.MESSAGE, "[SERVER]", getRooms());
                    mainSession.writeMessage(roomsList);
                    break;

                case "/createroom":
                    if (!validateCommand(commands)) {
                        Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", "Failed to create new room! Type '/help' for more information");
                        mainSession.writeMessage(errorMessage);
                        break;
                    }
                    String nameOfRoom = commands[1];
                    Room room = new Room(nameOfRoom);
                    rooms.add(room);
                    Message roomCreateMessage = new Message(MessageType.MESSAGE, "[SERVER]", nameOfRoom + " room was created");
                    mainSession.writeMessage(roomCreateMessage);
                    break;

                case "/join":
                    if (!validateCommand(commands)) {
                        Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", "Failed to join to room! Type '/help' for more information");
                        mainSession.writeMessage(errorMessage);
                        break;
                    }
                    nameOfRoom = commands[1];
                    Room targetRoom = roomIsExist(nameOfRoom);
                    if (targetRoom == null) {
                        Message roomNotExist = new Message(MessageType.MESSAGE, "[SERVER]", nameOfRoom + " room is not exist");
                        mainSession.writeMessage(roomNotExist);
                        break;
                    }
                    Room currentRoom = mainSession.getRoom();
                    User currentUser = mainSession.getUser();

                    currentRoom.removeUser(currentUser);
                    currentUser.setRoom(targetRoom);
                    targetRoom.addUser(currentUser);
                    Message joinToRoom = new Message(MessageType.MESSAGE, "[SERVER]", "You joined to " + nameOfRoom + " room");
                    mainSession.writeMessage(joinToRoom);
                    break;
                default:
                    mainSession.writeMessage(new Message(MessageType.MESSAGE, "[SERVER]", "Command is not available yet. Type '/help' for more information"));
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to execute command", e);
        }
    }

    private Room roomIsExist(String nameOfRoom) {
        for (Room room : rooms) {
            if (room.getName().equals(nameOfRoom)) {
                return room;
            }
        }
        return null;
    }

    private boolean validateCommand(String[] commands) {
        if (commands.length != 2) {
            return false;
        }
        String nameOfRoom = commands[1];
        return validator.validateRoomName(nameOfRoom);
    }


    //broadcast to all
    private void echo(Message message) {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                Session session = (Session) key.attachment();
                try {
                    session.writeMessage(message);
                } catch (Exception e) {
                    logger.error("Cannot send message", e);
                }
            }
        }
    }

    //broadcast into singleRoom
    private void echo(Message message, Room room) {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                Session session = (Session) key.attachment();
                if (session.getRoom() == room) {
                    try {
                        session.writeMessage(message);
                    } catch (Exception e) {
                        logger.error("Cannot send message", e);
                    }
                }
            }
        }
    }
}
