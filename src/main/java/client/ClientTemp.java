package client;

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
            ByteBuffer byteBuffer = ByteBuffer.allocate(140);
            try {
                socketChannel.read(byteBuffer);
            } catch (IOException e) {
                logger.error("Connection closed");
                break;
            }

            //todo fix reading fullsize broadcast msg

            byte[] tempByte = new byte[byteBuffer.capacity() - byteBuffer.remaining()];
            byteBuffer.rewind();
            byteBuffer.get(tempByte);
            String result = new String(tempByte);
            logger.info(result);
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {

        ClientTemp clientTemp = new ClientTemp();
        Thread clientThread = new Thread(clientTemp);
        clientThread.start();

        Scanner scanner = new Scanner(System.in);
        while (!Thread.currentThread().isInterrupted()) {
            String message = scanner.nextLine();
            if (!message.equals("/exit")) {
                try {
                    message += "\n";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
                    clientTemp.socketChannel.write(byteBuffer);
                    byteBuffer.clear();
                } catch (IOException exception) {
                    clientTemp.logger.error(exception.getMessage());
                    break;
                }
            } else {
                clientThread.interrupt();
                break;
            }
        }
    }
}
