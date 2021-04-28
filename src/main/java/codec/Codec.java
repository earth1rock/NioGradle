package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Codec {
    //fullMessageLength(4) + typeMessage(1) + nameLength(1) + messageLength(2)
    private static final byte HEADER_SIZE = 8;

    private static final short MAX_LENGTH = Short.MAX_VALUE;

    public ByteBuffer encode(Message message) {

        byte[] messageBytes = message.getMessage().getBytes(StandardCharsets.UTF_8);
        byte[] userNameBytes = message.getUserName().getBytes(StandardCharsets.UTF_8);
        int bytesLength = messageBytes.length;
        if (bytesLength <= MAX_LENGTH) {
            byte messageType = message.getMessageType();
            byte userNameLength = (byte) userNameBytes.length;
            short messageLength = (short) bytesLength;

            int fullSizeMessage = HEADER_SIZE +
                    userNameLength +
                    bytesLength;

            ByteBuffer messageBuffer = ByteBuffer.allocate(fullSizeMessage);

            //[int,byte,byte,short,text]
            messageBuffer.putInt(fullSizeMessage);
            messageBuffer.put(messageType);
            messageBuffer.put(userNameLength);
            messageBuffer.putShort(messageLength);
            messageBuffer.put(userNameBytes);
            messageBuffer.put(messageBytes);

            return messageBuffer;
        } else {
            throw new IllegalArgumentException("Max message size = " + MAX_LENGTH + " | Your message size is " + message.getMessage().length());
        }
    }

    public boolean canDecode(ByteBuffer buffer) {
        if (buffer.remaining() < 4) {
            return false;
        }
        int fullSizeMessage = buffer.getInt();
        buffer.rewind();
        if (fullSizeMessage < 1 || fullSizeMessage > buffer.limit()) {
            return false;
        }
        buffer.position(fullSizeMessage);
        return true;
    }

    //[int,byte,byte,short,text]
    public Message decode(ByteBuffer buffer) {
        int fullSizeLength = buffer.getInt();
        byte messageType = buffer.get();
        byte userNameLength = buffer.get();
        short messageLength = buffer.getShort();

        byte[] userName = new byte[userNameLength];
        buffer.get(userName);
        String userNameString = new String(userName);

        byte[] message = new byte[messageLength];
        buffer.get(message);
        String messageString = new String(message);

        return new Message(messageType, userNameString, messageString);
    }
}
