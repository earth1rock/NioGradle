package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

public class CommandErrorSyntax implements Command{
    private final static String syntaxError = "Syntax error: ";
    private final static String helpMessageString = " Type '/help' for more information";
    private final String infoMsg;

    public CommandErrorSyntax(String infoMsg) {
        this.infoMsg = infoMsg;
    }

    @Override
    public void execute(Session session) throws Exception {
        Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", syntaxError + infoMsg + helpMessageString);
        session.writeMessage(errorMessage);
    }
}
