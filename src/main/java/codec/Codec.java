package codec;

import message.Message;

import java.nio.ByteBuffer;

public class Codec {

    private static final short MAX_LENGTH = Short.MAX_VALUE - 1;
    //fullSize(int) + messageType(byte) + userNameLength(byte) + messageLength(short)
    private static final byte HEADER_SIZE = 8;

    public static ByteBuffer encode(Message message) throws Exception {

        if (message.getMessage().length() <= MAX_LENGTH) {
            byte messageType = message.getMessageType();
            byte userNameLength = (byte) message.getUserName().length();
            short messageLength = (short) message.getMessage().length();

            int fullSizeBufferLength = HEADER_SIZE + userNameLength + messageLength;
            ByteBuffer messageBuffer = ByteBuffer.allocate(fullSizeBufferLength);

            messageBuffer.putInt(fullSizeBufferLength);
            messageBuffer.put(messageType);
            messageBuffer.put(userNameLength);
            messageBuffer.putShort(messageLength);
            messageBuffer.put(message.getUserName().getBytes());
            messageBuffer.put(message.getMessage().getBytes());

            return messageBuffer;
        } else {
            throw new Exception("Max message size = " + MAX_LENGTH + " | Your message size is " + message.getMessage().length());
        }
    }

    public static Message decode(ByteBuffer buffer) throws Exception {
        try {
            int fullSizeMessageLength = buffer.getInt();
            byte messageType = buffer.get();
            byte userNameLength = buffer.get();
            short messageLength = buffer.getShort();

            byte[] userNameLengthBuffer = new byte[userNameLength];
            buffer.get(userNameLengthBuffer);

            byte[] messageLengthBuffer = new byte[messageLength];
            buffer.get(messageLengthBuffer);

            return new Message(messageType,
                    new String(userNameLengthBuffer),
                    new String(messageLengthBuffer));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
