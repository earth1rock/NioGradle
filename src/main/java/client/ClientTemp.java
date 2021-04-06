package client;

import codec.Reader;
import codec.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    InetSocketAddress inetSocketAddress;
    SocketChannel socketChannel;
    private final Logger logger = LoggerFactory.getLogger(ClientTemp.class);

    ClientTemp() throws IOException {
        inetSocketAddress = new InetSocketAddress("localhost", 2222);
        socketChannel = SocketChannel.open(inetSocketAddress);
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Reader reader = new Reader(socketChannel);
                String result = reader.readMessage();
                logger.info(result);
            }
            catch (Exception e) {
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

    public static void main(String[] args) throws IOException {

        ClientTemp clientTemp = new ClientTemp();
        Thread clientThread = new Thread(clientTemp);
        clientThread.start();

        Scanner scanner = new Scanner(System.in);
        while (!Thread.currentThread().isInterrupted()) {
            String message = scanner.nextLine();
            if (!message.equals("/exit") && clientTemp.socketChannel.isOpen()) {
                try {
                    Writer writer = new Writer(clientTemp.socketChannel);
                    writer.writeMessage(message);
                } catch (Exception exception) {
                    clientTemp.logger.error(exception.getMessage());
                }
            } else {
                clientThread.interrupt();
                break;
            }
        }
    }
}
