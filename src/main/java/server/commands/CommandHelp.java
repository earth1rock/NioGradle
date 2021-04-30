package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

import java.io.IOException;

public class CommandHelp implements Command {
    private final static String helpMessageString = "\nType '/list' for list users\n" +
            "Type '/rooms' for room list\n" +
            "Type '/createroom [name_of_room]' for create new room. [name_of_room] length must be in [7; 127]\n" +
            "Type '/join [name_of_room]' to join to room. [name_of_room] length must be in [7; 127]";

    @Override
    public void execute(Session session) throws IOException {
        Message helpMessage = new Message(MessageType.MESSAGE, "[SERVER]", helpMessageString);
        session.writeMessage(helpMessage);
    }
}
