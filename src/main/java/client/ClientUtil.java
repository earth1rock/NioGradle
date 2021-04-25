package client;

import message.Message;
import message.MessageType;


public final class ClientUtil {

    private ClientUtil() {}


    public static Message generateMessage(User user, String message) {

        if (message.equals("/exit")) {
            return new Message(MessageType.LEAVE, user.getName(), "");
        }
        if (message.startsWith("/")) {
            return new Message(MessageType.COMMAND, user.getName(), message);
        }
        return new Message(MessageType.MESSAGE, user.getName(), message);
    }

}
