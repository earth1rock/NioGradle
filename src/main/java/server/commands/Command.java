package server.commands;


import session.Session;


public interface Command {
    void execute(Session session) throws Exception;
}
