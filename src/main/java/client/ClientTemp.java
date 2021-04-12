package client;

import codec.Codec;
import codec.Reader;
import codec.Writer;
import message.Message;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Viewer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    private final SocketChannel socketChannel;
    private final static Logger logger = LoggerFactory.getLogger(ClientTemp.class);
    private final static Validator validator = new Validator();
    private final Codec codec = new Codec();
    private static Reader reader;
    private static User user;
    private final ClientHandler clientHandler;
    private final static Viewer viewer = new Viewer();

    ClientTemp(String name, String hostname, int port) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        user = new User(name);
        socketChannel = SocketChannel.open(inetSocketAddress);
        reader = new Reader(socketChannel, codec);
        clientHandler = new ClientHandler(socketChannel, user);
    }

    @Override
    public void run() {
        try {
            clientHandler.connect();
        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message result = reader.readMessage();
                viewer.print(result);
            } catch (Exception e) {
                logger.error("Failed to read message", e);
                break;
            }
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.error("Failed to close connection", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String userName = "";

        viewer.print("Input your nickname:");
        do {
            userName = scanner.nextLine().trim();
        } while (!validator.validate(userName));


        ClientTemp clientTemp = new ClientTemp(userName, "localhost", 2222);
        Thread clientThread = new Thread(clientTemp);
        clientThread.start();

        while (!Thread.currentThread().isInterrupted()) {
            String message = scanner.nextLine();
            if (clientTemp.socketChannel.isOpen()) {
                try {
                    clientTemp.clientHandler.write(message);
                } catch (Exception e) {
                    logger.error("Failed to send message", e);
                    break;
                }
            } else {
                logger.info("Connection is closed!");
                break;
            }
        }
    }
}
