package codec;

import java.nio.ByteBuffer;

public class Codec {

    private static final short MAX_LENGTH = Short.MAX_VALUE;
    private static final byte HEADER_SIZE = 2;

    public static ByteBuffer encode(String message) throws Exception {

        if (message.length() < MAX_LENGTH) {
            short messageLength = (short) message.length();
            ByteBuffer messageBuffer = ByteBuffer.allocate(HEADER_SIZE + messageLength);
            messageBuffer.putShort(messageLength);
            messageBuffer.put(message.getBytes());
            return messageBuffer;
        } else {
            throw new Exception("Max message size = " + MAX_LENGTH + " | Your message size is " + message.length());
        }
    }

    public static String decode(ByteBuffer buffer) throws Exception {
        short messageLength = buffer.getShort();
        if (messageLength < MAX_LENGTH) {
            byte[] bytes = new byte[messageLength];
            buffer.get(bytes);
            return new String(bytes);
        }
        else {
            throw new Exception("Cannot decode message");
        }
    }

}
