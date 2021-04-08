package client;

import codec.Reader;
import codec.Writer;
import message.Message;
import message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    private static User user;
    private static Writer writer;
    InetSocketAddress inetSocketAddress;
    SocketChannel socketChannel;
    private final Logger logger = LoggerFactory.getLogger(ClientTemp.class);

    ClientTemp(String name, int port) {
        inetSocketAddress = new InetSocketAddress("localhost", port);
        user = new User(name);
    }

    private void connect() throws Exception {
        socketChannel = SocketChannel.open(inetSocketAddress);
        writer = new Writer(socketChannel);
        writer.writeMessage(new Message(MessageType.JOIN, user.getName(), ""));
    }

    @Override
    public void run() {
        Reader reader = new Reader(socketChannel);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                logger.info(reader.readMessage().getFormattedMessage());
            } catch (Exception e) {
                logger.error(e.toString());
                break;
            }
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String userName = "";
        System.out.println("Input your nickname:");

        while (!Thread.currentThread().isInterrupted()) {
            userName = scanner.nextLine().trim();
            if (userName.length() > 6 && userName.length() < 127) {
                break;
            } else {
                System.out.println("Nickname length must be in [7;126]");
            }
        }

        ClientTemp clientTemp = new ClientTemp(userName, 2222);
        clientTemp.connect();

        Thread clientThread = new Thread(clientTemp);
        clientThread.start();

        //Writer writer = new Writer(clientTemp.socketChannel);
        while (!Thread.currentThread().isInterrupted()) {
            String inputMessage = scanner.nextLine();
            if (!inputMessage.equals("/exit") && clientTemp.socketChannel.isOpen()) {
                try {
                    Message message = new Message(MessageType.MESSAGE, user.getName(), inputMessage);
                    writer.writeMessage(message);
                } catch (Exception e) {
                    clientTemp.logger.error(e.toString());
                }
            } else {
                try {
                    writer.writeMessage(new Message(MessageType.LEAVE, user.getName(), ""));
                } catch (Exception e) {
                    clientTemp.logger.info(e.toString());
                }
                clientThread.interrupt();
                break;
            }
        }
    }
}
