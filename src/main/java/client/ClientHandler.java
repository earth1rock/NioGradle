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

    public void doTask(Session session, Message message) throws IOException {
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

    public Message generateMessage(User user, String message) {
        if (message.equals("/exit")) {
            return new Message(MessageType.LEAVE, user.getName(), "");
        } else if (message.startsWith("/")) {
            return new Message(MessageType.COMMAND, user.getName(), message);
        } else {
            return new Message(MessageType.MESSAGE, user.getName(), message);
        }
    }

    public void onConnected(Session session) throws Exception {
        Message joinMessage = new Message(MessageType.JOIN, user.getName(), "");
        session.writeMessage(joinMessage);
    }
}
