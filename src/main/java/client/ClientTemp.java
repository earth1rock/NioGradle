package client;

import codec.Codec;
import codec.Session;
import message.Message;
import message.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Viewer;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ClientTemp.class);
    private final ClientHandler clientHandler;


    ClientTemp(String name, String hostname, int port, Codec codec, Viewer viewer) throws Exception {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        User user = new User(name);
        SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
        Session session = new Session(socketChannel, codec, user);
        clientHandler = new ClientHandler(session, viewer);
    }

    public ClientHandler getHandler() {
        return clientHandler;
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
                Message result = clientHandler.read();
                clientHandler.doTask(result);
            } catch (Exception e) {
                logger.error("Failed to read message. Probably connection is lost", e);
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String userName = "";
        Codec codec = new Codec();
        MessageFormatter formatter = new MessageFormatter();
        Viewer viewer = new Viewer(formatter);
        Validator validator = new Validator(viewer);

        viewer.print("Input your nickname:");
        do {
            userName = scanner.nextLine().trim();
        } while (!validator.validate(userName));


        ClientTemp clientTemp = new ClientTemp(userName, "localhost", 2222, codec, viewer);
        Thread clientThread = new Thread(clientTemp);
        clientThread.start();
        ClientHandler clientHandler = clientTemp.getHandler();

        while (!Thread.currentThread().isInterrupted()) {
            String message = scanner.nextLine();
            try {
                clientHandler.write(message);
            } catch (Exception e) {
                logger.error("Failed to send message", e);
                break;
            }
        }
    }
}
