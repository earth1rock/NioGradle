package server;

import codec.Codec;
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
    private final static Message welcomeMessage = new Message(MessageType.WELCOME, "[SERVER]", "Welcome to NioChat!");
    private final static Message infoMessage = new Message(MessageType.WELCOME, "[SERVER]", "If you need some help just type /help");
    private final int port;
    private final static Logger logger = LoggerFactory.getLogger(ServerTemp.class);
    private final ServerHandler serverHandler;
    private final Codec codec = new Codec();

    ServerTemp(int port) throws IOException {
        this.port = port;
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        serverSocketChannel.bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverHandler = new ServerHandler(selector);
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
            logger.error("ERROR", e);
        } finally {
            try {
                selector.close();
                serverSocketChannel.close();
            } catch (IOException e) {
                logger.error("Failed to close selector/serverSocketChannel", e);
            }
        }
    }

    private void accept(SelectionKey key) throws Exception {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        serverHandler.doTask(socketChannel, welcomeMessage);
        serverHandler.doTask(socketChannel, infoMessage);
    }

    private void read(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        Reader reader = new Reader(socketChannel, codec);
        try {
            Message inputMessage = reader.readMessage();
            serverHandler.doTask(socketChannel, inputMessage);
        } catch (Exception e) {
            socketChannel.close();
            logger.info("Cannot read message", e);
        }
    }
}

