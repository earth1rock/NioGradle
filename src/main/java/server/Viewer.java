package server;

import message.Message;
import message.MessageFormatter;

import java.util.Objects;

public class Viewer {
    private final MessageFormatter formatter;

    public Viewer(MessageFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "Formatter must not be null");
    }

    public void print(Message message) {
        String formattedString = formatter.formatMessage(message);
        System.out.println(formattedString);
    }

    public void print(String message) {
        System.out.println(message);
    }
}
