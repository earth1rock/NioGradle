package client;

import codec.Session;
import message.Message;
import message.MessageType;
import server.Viewer;

import java.io.IOException;

public class ClientHandler {

    private final Session session;
    private final User user;
    private final Viewer viewer;

    public ClientHandler(Session session, Viewer viewer) {
        this.session = session;
        this.user = session.getUser();
        this.viewer = viewer;
    }

    public Message read() throws Exception {
        return session.readMessage();
    }

    public void doTask(Message message) throws IOException {
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

    public void write(String message) throws Exception {
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

    public void connect() throws Exception {
        Message joinMessage = new Message(MessageType.JOIN, user.getName(), "");
        session.writeMessage(joinMessage);
    }
}
