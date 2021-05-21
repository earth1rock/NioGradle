package message;

public class Message {
    private byte messageType;
    private final String userName;
    private final String message;

    public Message(byte messageType, String userName, String message) {
        this.messageType = messageType;
        this.userName = userName;
        this.message = message;
    }
    public byte getMessageType() {
        return messageType;
    }
    public String getUserName() {
        return userName;
    }
    public String getMessage() {
        return message;
    }
    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }


    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
                ", userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
