package server;

import client.User;
import codec.Codec;
import codec.Writer;
import message.Message;
import message.MessageFormatter;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ServerHandler {
    private final Codec codec = new Codec();
    private final Selector selector;
    private final static Viewer viewer = new Viewer();
    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private User user;

    public ServerHandler(Selector selector) {
        this.selector = selector;
    }

    public void doTask(SocketChannel socketChannel, Message message) throws Exception {
        Writer writer = new Writer(socketChannel, codec);
        String address = socketChannel.socket().getInetAddress().toString() + ":" + socketChannel.socket().getPort();

        switch (message.getMessageType()) {

            case MessageType.MESSAGE:
                echo(message);
                String formattedMessage = MessageFormatter.formatMessage(message);
                viewer.print(formattedMessage);
                break;
            case MessageType.WELCOME:
                writer.writeMessage(message);
                break;
            case MessageType.JOIN:
                Message registerMessage = new Message(MessageType.MESSAGE, "[SERVER]", message.getUserName() + " connected to the server");
                echo(registerMessage);
                user = new User(message.getUserName());
                //todo attach to default room
                socketChannel.keyFor(selector).attach(user);
                formattedMessage = MessageFormatter.formatForLogs(registerMessage) + " | " + address;
                logger.info(formattedMessage);
                break;
            case MessageType.LEAVE:
                Message msg = new Message(MessageType.MESSAGE, "[SERVER]", message.getUserName() + " disconnected from the server");
                echo(msg);
                socketChannel.close();
                formattedMessage = MessageFormatter.formatForLogs(msg) + " | " + address;
                logger.info(formattedMessage);
                break;
            case MessageType.COMMAND:
                executeCommand(message, writer);
                break;
        }
    }

    private void executeCommand(Message message, Writer writer) {

        String command = message.getMessage();

        try {
            switch (command) {
                case "/list":
                    for (SelectionKey key : selector.keys()) {
                        if (key.isValid() && key.channel() instanceof SocketChannel) {
                            user = (User) key.attachment();
                            Message tempMessage = new Message(MessageType.MESSAGE, "[SERVER]", user.getName());
                            writer.writeMessage(tempMessage);
                        }
                    }
                    break;

                default:
                    writer.writeMessage(new Message(MessageType.MESSAGE, "[SERVER]", "Command is not available yet"));
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to execute command", e);
        }
    }

    private void echo(Message message) throws IOException {
        Writer writer;
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                writer = new Writer(socketChannel, codec);
                try {
                    writer.writeMessage(message);
                } catch (Exception e) {
                    socketChannel.close();
                    logger.error("Cannot write message", e);
                }
            }
        }
    }


}
