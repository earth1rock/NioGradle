package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

public class CommandHelp implements Command{
    private final static String helpMessageString = "\nType '/rooms' for room list\n" +
            "Type '/createroom [name_of_room]' for create new room. [name_of_room] length must be in [7; 127]\n" +
            "Type '/join [name_of_room]' to join to room";
    @Override
    public void execute(Session session) throws Exception {
        Message helpMessage = new Message(MessageType.MESSAGE, "[SERVER]", helpMessageString);
        session.writeMessage(helpMessage);
    }
}
