package server;

import client.User;
import client.Validator;
import room.Room;
import server.commands.*;
import session.Session;
import message.Message;
import message.MessageFormatter;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ServerHandler {
    private final Viewer viewer;
    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final static Message welcomeMessage = new Message(MessageType.WELCOME, "[SERVER]", "Welcome to NioChat!");
    private final static Message infoMessage = new Message(MessageType.WELCOME, "[SERVER]", "If you need some help just type /help");
    private final Set<Session> sessionSet;
    private final Set<Room> rooms;
    private final Validator validator;
    private final Room DEFAULT_ROOM = new Room("DEFAULT");

    public ServerHandler(Viewer viewer) throws NullPointerException {
        this.viewer = Objects.requireNonNull(viewer);
        sessionSet = new HashSet<>();
        rooms = new HashSet<>();
        rooms.add(DEFAULT_ROOM);
        validator = new Validator(viewer);
    }

    public void onSessionCreate(Session session) throws Exception {
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

    public void doTask(Session session, Message message) throws Exception {

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

    //todo /join
    private void executeCommand(Message message, Session mainSession) {

        String editedMsg = message.getMessage().trim().replaceAll("\\s+", " ");
        String[] commands = editedMsg.split(" ", 2);
        String command = commands[0];

        try {
            switch (command) {
                case "/list":
                    Command commandList = new CommandList(sessionSet);
                    commandList.execute(mainSession);
                    break;

                case "/help":
                    Command commandHelp = new CommandHelp();
                    commandHelp.execute(mainSession);
                    break;

                case "/rooms":
                    Command commandRooms = new CommandRooms(rooms);
                    commandRooms.execute(mainSession);
                    break;

                case "/createroom":
                    Command commandCreateRoom = new CommandCreateRoom(validator, commands, rooms);
                    commandCreateRoom.execute(mainSession);
                    break;

                case "/join":
                    Command commandJoin = new CommandJoin(validator, commands, rooms);
                    commandJoin.execute(mainSession);
                    break;

                default:
                    mainSession.writeMessage(new Message(MessageType.MESSAGE, "[SERVER]", "Command is not available yet"));
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to execute command", e);
        }
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
