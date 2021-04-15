package server;

import client.User;
import codec.Codec;
import message.MessageFormatter;
import room.Room;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Codec codec = new Codec();
        MessageFormatter formatter = new MessageFormatter();
        Viewer viewer = new Viewer(formatter);

        HashSet<Room> rooms = new HashSet<>();
        rooms.add(User.getDefaultRoom());

        ServerTemp server = new ServerTemp(2222, codec, viewer, rooms);
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
