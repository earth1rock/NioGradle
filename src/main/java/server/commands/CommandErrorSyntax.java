package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

public class CommandErrorSyntax implements Command{
    private final static String helpMessageString = "Syntax error. Type '/help' for more information";

    @Override
    public void execute(Session session) throws Exception {
        Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", helpMessageString);
        session.writeMessage(errorMessage);
    }
}
