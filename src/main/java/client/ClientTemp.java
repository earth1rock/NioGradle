package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTemp implements Runnable {

    InetSocketAddress inetSocketAddress;
    SocketChannel socketChannel;

    ClientTemp() throws IOException {
        inetSocketAddress = new InetSocketAddress("localhost", 2222);
        socketChannel = SocketChannel.open(inetSocketAddress);
    }

    @Override
    public void run() {
        while (true) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(140);
            try {
                socketChannel.read(byteBuffer);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(0);
                break;
            }
            String result = new String(byteBuffer.array()).trim();
            System.out.println(result);
        }
    }

    public static void main(String[] args) throws IOException {

        ClientTemp clientTemp = new ClientTemp();
        new Thread(clientTemp).start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            try {
                ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
                clientTemp.socketChannel.write(byteBuffer);
                byteBuffer.clear();
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
                break;
            }
        }
    }
}

