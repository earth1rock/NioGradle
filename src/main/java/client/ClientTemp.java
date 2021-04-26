package client;

import codec.Codec;
import message.MessageType;
import session.Session;
import message.Message;
import message.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Viewer;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ClientTemp.class);
    private final ClientHandler clientHandler;
    private final Session session;

    ClientTemp(User user, String hostname, int port, Codec codec, ClientHandler clientHandler) throws Exception {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
        this.session = new Session(socketChannel, codec, user);
        this.clientHandler = Objects.requireNonNull(clientHandler, "ClientHandler must not be null");
    }

    @Override
    public void run() {

        try {
            clientHandler.onConnected(session);
        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message result = session.readMessage();
                clientHandler.doTask(session, result);
            } catch (Exception e) {
                logger.error("Failed to read message. Probably connection is lost", e);
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String userName;
        Codec codec = new Codec();
        MessageFormatter formatter = new MessageFormatter();
        Viewer viewer = new Viewer(formatter);
        Validator validator = new Validator(viewer);

        viewer.print("Input your nickname:");
        do {
            userName = scanner.nextLine().trim();
        } while (!validator.validateNickname(userName));

        User user = new User(userName);
        ClientHandler clientHandler = new ClientHandler(user, viewer);

        ClientTemp clientTemp = new ClientTemp(user, "localhost", 2222, codec, clientHandler);
        Thread clientThread = new Thread(clientTemp);
        clientThread.start();
        Session session = clientTemp.session;


        while (!Thread.currentThread().isInterrupted()) {
            String message = scanner.nextLine();
            try {
                Message generatedMessage = ClientUtil.generateMessage(user, message);
                session.writeMessage(generatedMessage);
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException) {
                    logger.error("Failed to send message", e);
                    continue;
                }
                logger.error("Failed to send message", e);
                break;
            }
        }
    }
}
