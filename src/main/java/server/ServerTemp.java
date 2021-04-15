package server;

import codec.Codec;
import codec.Session;
import message.Message;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import room.Room;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerTemp implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final int port;
    private final ServerHandler serverHandler;
    private final Codec codec;
    private final InetSocketAddress inetSocketAddress;
    private final static Message welcomeMessage = new Message(MessageType.WELCOME, "[SERVER]", "Welcome to NioChat!");
    private final static Message infoMessage = new Message(MessageType.WELCOME, "[SERVER]", "If you need some help just type /help");
    private final static Logger logger = LoggerFactory.getLogger(ServerTemp.class);


    ServerTemp(int port, Codec codec, Viewer viewer, Set<Room> rooms) throws IOException {
        this.port = port;
        this.codec = codec;
        inetSocketAddress = new InetSocketAddress(port);
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverHandler = new ServerHandler(selector, viewer, rooms);
    }

    public void init() throws IOException {
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
            logger.error("ERROR ", e);
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
        socketChannel.configureBlocking(false);
        Session session = new Session(socketChannel, codec);
        session.writeMessage(welcomeMessage);
        session.writeMessage(infoMessage);
        socketChannel.register(selector, SelectionKey.OP_READ, session);
    }


    private void read(SelectionKey key) {
        Session session = (Session) key.attachment();
        try {
            Message inputMessage = session.readMessage();
            serverHandler.doTask(session, inputMessage);
        } catch (Exception e) {
            logger.info("Failed to read message", e);
        }
    }
}

