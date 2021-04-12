package client;

import codec.Codec;
import codec.Writer;
import message.Message;
import message.MessageType;

import java.nio.channels.SocketChannel;

public class ClientHandler {

    private final Writer writer;
    private final Codec codec = new Codec();
    private final SocketChannel socketChannel;
    private final User user;

    public ClientHandler(SocketChannel socketChannel, User user) {
        this.socketChannel = socketChannel;
        writer = new Writer(socketChannel, codec);
        this.user = user;
    }

    public void write(String message) throws Exception {
        if (message.equals("/exit")) {
            Message leaveMessage = new Message(MessageType.LEAVE, user.getName(), "");
            writer.writeMessage(leaveMessage);
        } else if (message.startsWith("/")) {
            Message commandMessage = new Message(MessageType.COMMAND, user.getName(), message);
            writer.writeMessage(commandMessage);
        } else {
            Message messageObj = new Message(MessageType.MESSAGE, user.getName(), message);
            writer.writeMessage(messageObj);
        }
    }

    public void connect() throws Exception {
        Message registerMessage = new Message(MessageType.JOIN, user.getName(), "");
        write(registerMessage);
    }

    public void write(Message message) throws Exception {
        writer.writeMessage(message);
    }
}
