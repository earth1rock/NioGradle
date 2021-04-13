package server;

import codec.Codec;
import message.MessageFormatter;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Codec codec = new Codec();
        MessageFormatter formatter = new MessageFormatter();
        Viewer viewer = new Viewer(formatter);
        ServerTemp server = new ServerTemp(2222, codec, viewer);
        server.init();

        Thread serverThread = new Thread(server);
        serverThread.start();

        Scanner scanner = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()) {
            String input = scanner.nextLine();
            if (input.equals("shutdown")) {
                serverThread.interrupt();
                break;
            }
        }
    }
}
