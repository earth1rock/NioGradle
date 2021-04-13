package server;

import client.User;
import codec.Session;
import message.Message;
import message.MessageFormatter;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ServerHandler {
    private final Selector selector;
    private final Viewer viewer;
    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public ServerHandler(Selector selector, Viewer viewer) {
        this.selector = selector;
        this.viewer = viewer;
    }

    public void doTask(Session session, Message message) throws Exception {

        switch (message.getMessageType()) {

            case MessageType.MESSAGE:
                echo(message);
                viewer.print(message);
                break;
            case MessageType.WELCOME:
                session.writeMessage(message);
                break;
            case MessageType.JOIN:
                //todo attach to default room
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

    //todo /join
    private void executeCommand(Message message, Session mainSession) {

        String command = message.getMessage();

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
                default:
                    mainSession.writeMessage(new Message(MessageType.MESSAGE, "[SERVER]", "Command is not available yet"));
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to execute command", e);
        }
    }

    private void echo(Message message) {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                Session session = (Session) key.attachment();
                try {
                    session.writeMessage(message);
                } catch (Exception e) {
                    //todo session close
                    logger.error("Cannot write message", e);
                }
            }
        }
    }
}
