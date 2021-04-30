package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

import java.io.IOException;

public class CommandDefault implements Command {

    private final static String DEFAULT_MESSAGE = "Command is not available yet";

    @Override
    public void execute(Session session) throws IOException {
        Message helpMessage = new Message(MessageType.MESSAGE, "[SERVER]", DEFAULT_MESSAGE);
        session.writeMessage(helpMessage);

    }
}
