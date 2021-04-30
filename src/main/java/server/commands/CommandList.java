package server.commands;


import message.Message;
import message.MessageType;
import session.Session;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandList implements Command{

    private final Set<Session> sessionSet;

    public CommandList(Set<Session> sessionSet) {
        this.sessionSet = Objects.requireNonNull(sessionSet, "Set of session is null");
    }


    @Override
    public void execute(Session session) throws IOException {
        String listUsersMessage = sessionSet.stream().map(s -> s.getUser().getName()).collect(Collectors.joining("\n"));
        Message tempMessage = new Message(MessageType.MESSAGE, "[SERVER]", "List of users:\n" + listUsersMessage);
        session.writeMessage(tempMessage);
    }
}
