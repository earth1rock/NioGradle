package server.commands;

import message.Message;
import message.MessageType;
import session.Session;

import java.io.IOException;
import java.util.Objects;

public class CommandErrorSyntax implements Command{
    private final static String syntaxError = "Syntax error: ";
    private final static String helpMessageString = " Type '/help' for more information";
    private final String infoMsg;

    public CommandErrorSyntax(String infoMsg) {
        this.infoMsg = Objects.requireNonNull(infoMsg, "Info message cannot be null");
    }

    @Override
    public void execute(Session session) throws IOException {
        Message errorMessage = new Message(MessageType.MESSAGE, "[SERVER]", syntaxError + infoMsg + helpMessageString);
        session.writeMessage(errorMessage);
    }
}
