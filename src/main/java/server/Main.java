package server;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerTemp server = new ServerTemp();
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
