package client;

import session.Session;
import message.Message;
import message.MessageType;
import server.Viewer;

import java.io.IOException;
import java.util.Objects;

public class ClientHandler {

    private final User user;
    private final Viewer viewer;

    public ClientHandler(User user, Viewer viewer) throws NullPointerException {
        this.user = Objects.requireNonNull(user, "User must not be null");
        this.viewer = Objects.requireNonNull(viewer, "Viewer must not be null");
    }

    public void doTask(Message message, Session session) throws IOException {
        switch (message.getMessageType()) {

            case MessageType.MESSAGE:
            case MessageType.WELCOME:
                viewer.print(message);
                break;

            case MessageType.LEAVE:
                session.close();
                viewer.print(message);
        }
    }

    public void write(String message, Session session) throws Exception {
        if (message.equals("/exit")) {
            Message leaveMessage = new Message(MessageType.LEAVE, user.getName(), "");
            session.writeMessage(leaveMessage);
        } else if (message.startsWith("/")) {
            Message commandMessage = new Message(MessageType.COMMAND, user.getName(), message);
            session.writeMessage(commandMessage);
        } else {
            Message messageObj = new Message(MessageType.MESSAGE, user.getName(), message);
            session.writeMessage(messageObj);
        }
    }

    public void onConnected(Session session) throws Exception {
        Message joinMessage = new Message(MessageType.JOIN, user.getName(), "");
        session.writeMessage(joinMessage);
    }
}
