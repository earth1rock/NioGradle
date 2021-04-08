package server;

import codec.Reader;
import codec.Writer;
import message.Message;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerTemp implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final int port = 2222;
    private final Logger logger = LoggerFactory.getLogger(ServerTemp.class);

    ServerTemp() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        serverSocketChannel.bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        logger.info("Server start | port: " + port);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                SelectionKey key;
                while (selectionKeyIterator.hasNext()) {
                    key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (key.isAcceptable()) this.accept(key);
                    if (key.isReadable()) this.read(key);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                selector.close();
                serverSocketChannel.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void accept(SelectionKey key) throws Exception {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress().toString() + ":" + socketChannel.socket().getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, address);
    }

    private void read(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        Reader reader = new Reader(socketChannel);
        try {
            Message inputMessage = reader.readMessage();
            doTask(socketChannel, inputMessage);
        } catch (Exception e) {
            socketChannel.close();
            logger.info(key.attachment() + " : " + e.getLocalizedMessage());
        }
    }

    private void doTask(SocketChannel socketChannel, Message inputMessage) throws Exception {
        Message message;
        Writer writer = new Writer(socketChannel);

        switch (inputMessage.getMessageType()) {
            case MessageType.MESSAGE:
                message = new Message(MessageType.MESSAGE, inputMessage.getUserName(), inputMessage.getMessage());
                echo(message);
                logger.info(message.getFormattedMessage());
                break;
            case MessageType.JOIN:
                writer.writeMessage(new Message(MessageType.WELCOME, "[SERVER]", "You're welcome"));
                message = new Message(MessageType.MESSAGE, "[SERVER]", inputMessage.getUserName() + " connected to the server");
                echo(message);
                logger.info(message.getFormattedMessage()
                        + " | " + socketChannel.socket().getInetAddress()
                        + ":" + socketChannel.socket().getPort());
                break;
            case MessageType.LEAVE:
                message = new Message(MessageType.MESSAGE, "[SERVER]", inputMessage.getUserName() + " disconnected from the server");
                echo(message);
                logger.info(message.getFormattedMessage()
                        + " | " + socketChannel.socket().getInetAddress()
                        + ":" + socketChannel.socket().getPort());
                socketChannel.close();
                break;
            case MessageType.COMMAND:
                //todo /list /join
                break;
        }
    }

    private void echo(Message message) throws IOException {
        Writer writer;
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                writer = new Writer(socketChannel);
                try {
                    writer.writeMessage(message);
                } catch (Exception e) {
                    socketChannel.close();
                    logger.error(e.toString());
                }
            }
        }
    }
}

