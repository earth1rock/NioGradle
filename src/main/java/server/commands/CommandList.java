package server.commands;


import message.Message;
import message.MessageType;
import session.Session;

import java.util.Set;

public class CommandList implements Command{

    private final Set<Session> sessionSet;

    public CommandList(Set<Session> sessionSet) {
        this.sessionSet = sessionSet;
    }


    @Override
    public void execute(Session session) throws Exception {
        for (Session singleSession : sessionSet) {
            Message tempMessage = new Message(MessageType.MESSAGE, "[SERVER]", singleSession.getUser().getName());
            session.writeMessage(tempMessage);
        }
    }
}
