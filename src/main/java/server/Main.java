package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerTemp server = new ServerTemp();
        new Thread(server).start();
    }
}
