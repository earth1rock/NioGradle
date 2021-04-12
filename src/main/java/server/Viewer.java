package server;

import message.Message;
import message.MessageFormatter;

public class Viewer {

    public void print(Message message) {
        String formattedString = MessageFormatter.formatMessage(message);
        System.out.println(formattedString);
    }

    public void print(String message) {
        System.out.println(message);
    }
}
