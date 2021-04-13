package server;

import message.Message;
import message.MessageFormatter;

public class Viewer {
    private final MessageFormatter formatter;

    public Viewer(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    public void print(Message message) {
        String formattedString = formatter.formatMessage(message);
        System.out.println(formattedString);
    }

    public void print(String message) {
        System.out.println(message);
    }
}
