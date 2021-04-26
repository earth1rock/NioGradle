package server;

import codec.Codec;
import session.Session;
import message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;

public class ServerTemp implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final int port;
    private final ServerHandler serverHandler;
    private final Codec codec;
    private final InetSocketAddress inetSocketAddress;
    private final static Logger logger = LoggerFactory.getLogger(ServerTemp.class);


    ServerTemp(int port, Codec codec, ServerHandler serverHandler) throws Exception {
        this.port = port;
        this.codec = Objects.requireNonNull(codec, "Codec must not be null");
        inetSocketAddress = new InetSocketAddress(port);
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        this.serverHandler = Objects.requireNonNull(serverHandler, "ServerHandler must not be null");
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
        socketChannel.register(selector, SelectionKey.OP_READ, session);
        serverHandler.onSessionCreate(session);
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

