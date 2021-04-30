package server.commands;


import session.Session;

import java.io.IOException;


public interface Command {
    void execute(Session session) throws IOException;
}
