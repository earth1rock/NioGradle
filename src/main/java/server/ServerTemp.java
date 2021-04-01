package server;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class ServerTemp implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to NioChat!\n".getBytes());
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

        try {
            logger.info("Server start | port: " + port);

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
        } catch (IOException e) {
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

    private void accept(SelectionKey key) throws IOException {
        //accept блокирует до тех пор пока не будет получено соединение
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = (new StringBuilder(socketChannel.socket().getInetAddress().toString()))
                .append(":")
                .append(socketChannel.socket().getPort()).toString();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, address);
        echo("Connected client: " + address + "\n");
        socketChannel.write(welcomeBuf);
        welcomeBuf.rewind();
        logger.info("Connect client: " + address);
    }

    private void read(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(140);
        String result = key.attachment() +
                ": " +
                "disconnected\r\n";
        try {
            StringBuilder builder = new StringBuilder();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                builder.append(new String(bytes));
                byteBuffer.clear();
            }
            result = key.attachment() +
                    ": " +
                    builder.toString();
            logger.info(result.substring(0, result.length() - 1));
        } catch (Exception e) {
            logger.info(result.substring(0, result.length() - 1));
            socketChannel.close();
        }
        echo(result);
    }

    private void echo(String message) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel sch = (SocketChannel) key.channel();
                sch.write(byteBuffer);
                byteBuffer.rewind();
            }
        }
    }
}

