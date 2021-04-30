package server;

import client.User;
import client.Validator;
import message.IllegalLengthOfMessageException;
import room.Room;
import server.commands.Command;
import server.commands.CommandCreateRoom;
import server.commands.CommandDefault;
import server.commands.CommandErrorSyntax;
import server.commands.CommandHelp;
import server.commands.CommandJoin;
import server.commands.CommandList;
import server.commands.CommandRooms;
import session.Session;
import message.Message;
import message.MessageFormatter;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class ServerHandler {
    private final Viewer viewer;
    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final static Message welcomeMessage = new Message(MessageType.WELCOME, "[SERVER]", "Welcome to NioChat!");
    private final static Message infoMessage = new Message(MessageType.WELCOME, "[SERVER]", "If you need some help just type /help");
    private final Set<Session> sessionSet;
    private final Set<Room> rooms;
    private final Validator validator;
    private final Room DEFAULT_ROOM = new Room("DEFAULT");
    private final static Function<String[], Command> DEFAULT_COMMAND = args -> new CommandDefault();
    private final Map<String, Function<String[], Command>> commandsFactory;

    public ServerHandler(Viewer viewer) {
        this.viewer = Objects.requireNonNull(viewer);
        sessionSet = new HashSet<>();
        rooms = new HashSet<>();
        rooms.add(DEFAULT_ROOM);
        validator = new Validator(viewer);

        commandsFactory = Map.of(
                "/list", args -> {
                    if (args.length == 1) return new CommandList(sessionSet);
                    throw new IllegalArgumentException("Command '/list' cannot have any args.");
                },
                "/help", args -> {
                    if (args.length == 1) return new CommandHelp();
                    throw new IllegalArgumentException("Command '/help' cannot have any args.");
                },
                "/rooms", args -> {
                    if (args.length == 1) return new CommandRooms(rooms);
                    throw new IllegalArgumentException("Command '/rooms' cannot have any args.");
                },
                "/join", args -> {
                    if (args.length != 2) throw new IllegalArgumentException("Command '/join' must have syntax '/join [room_name]'.");
                    String nameOfRoom = args[1];
                    if (!validator.validateRoomName(nameOfRoom)) throw new IllegalArgumentException("Invalid name for room.");
                    return new CommandJoin(nameOfRoom, rooms);
                },
                "/createroom", args -> {
                    if (args.length != 2) throw new IllegalArgumentException("Command '/createroom' must have syntax '/createroom [room_name]'.");
                    String nameOfRoom = args[1];
                    if (!validator.validateRoomName(nameOfRoom)) throw new IllegalArgumentException("Invalid name for room.");
                    return new CommandCreateRoom(nameOfRoom, rooms);
                }
        );
    }

    public void onSessionCreate(Session session) throws IOException {
        sessionSet.add(session);
        session.writeMessage(welcomeMessage);
        session.writeMessage(infoMessage);
    }

    public void onSessionClosed(Session session) {
        User leaveUser = session.getUser();
        session.getRoom().removeUser(leaveUser);
        sessionSet.remove(session);
        Message leaveMessage = new Message(MessageType.MESSAGE, "[SERVER]", session.getUser().getName() + " disconnected from the server");
        echo(leaveMessage);
        String formattedMessage = MessageFormatter.formatForLogs(leaveMessage);
        logger.info(formattedMessage);
    }

    public void doTask(Session session, Message message) throws IOException {

        switch (message.getMessageType()) {

            case MessageType.MESSAGE:
                Room room = session.getRoom();
                echo(message, room);
                viewer.print(message);
                break;
            case MessageType.WELCOME:
                session.writeMessage(message);
                break;
            case MessageType.JOIN:
                User newUser = new User(message.getUserName());
                newUser.setRoom(DEFAULT_ROOM);
                DEFAULT_ROOM.addUser(newUser);
                session.attachUser(newUser);
                Message joinMessage = new Message(MessageType.MESSAGE, "[SERVER]", message.getUserName() + " connected to the server");
                echo(joinMessage);
                String formattedMessage = MessageFormatter.formatForLogs(joinMessage);
                logger.info(formattedMessage);
                break;
            case MessageType.LEAVE:
                Message leaveMessage = new Message(MessageType.LEAVE, "[SERVER]", session.getUser().getName() + " disconnected from the server");
                session.writeMessage(leaveMessage);
                session.close();
                onSessionClosed(session);
                break;
            case MessageType.COMMAND:
                executeCommand(message, session);
                break;
        }
    }

    private void executeCommand(Message message, Session mainSession) throws IOException {

        String editedMsg = message.getMessage().trim().replaceAll("\\s+", " ");
        String[] partsOfCommand = editedMsg.split(" ", 2);
        String nameOfCommand = partsOfCommand[0];
        Command cmd;
        try {
            Function<String[], Command> factory = commandsFactory.getOrDefault(nameOfCommand, DEFAULT_COMMAND);
            cmd = factory.apply(partsOfCommand);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to get command", e);
            cmd = new CommandErrorSyntax(e.getMessage());
        }
        cmd.execute(mainSession);
    }


    private void echo(Message message) {

        for (Session session : sessionSet) {
            try {
                session.writeMessage(message);
            } catch (Exception e) {
                logger.error("Cannot write message", e);
            }
        }
    }

    private void echo(Message message, Room room) {

        for (Session session : sessionSet) {
            if (session.getRoom() == room) {
                try {
                    session.writeMessage(message);
                } catch (Exception e) {
                    logger.error("Cannot write message", e);
                }
            }
        }
    }
}
