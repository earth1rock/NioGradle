package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Codec {
    //fullMessageLength(4) + typeMessage(1) + nameLength(1) + messageLength(2)
    private static final byte HEADER_SIZE = 8;

    private static final short MAX_LENGTH = Short.MAX_VALUE;

    public ByteBuffer encode(Message message) throws Exception {

        int bytesLength = message.getMessage().getBytes(StandardCharsets.UTF_8).length;
        if (bytesLength <= MAX_LENGTH) {
            byte messageType = message.getMessageType();
            byte userNameLength = (byte) message.getUserName().length();
            short messageLength = (short) message.getMessage().length();

            int fullSizeMessage = HEADER_SIZE +
                    message.getUserName().getBytes(StandardCharsets.UTF_8).length +
                    message.getMessage().getBytes(StandardCharsets.UTF_8).length;

            ByteBuffer messageBuffer = ByteBuffer.allocate(fullSizeMessage);

            //[int,byte,byte,short,text]
            messageBuffer.putInt(fullSizeMessage);
            messageBuffer.put(messageType);
            messageBuffer.put(userNameLength);
            messageBuffer.putShort(messageLength);
            messageBuffer.put(message.getUserName().getBytes(StandardCharsets.UTF_8));
            messageBuffer.put(message.getMessage().getBytes(StandardCharsets.UTF_8));

            return messageBuffer;
        } else {
            throw new Exception("Max message size = " + MAX_LENGTH + " | Your message size is " + message.getMessage().length());
        }
    }

    public boolean canDecode(ByteBuffer buffer) {
        try {
            buffer.rewind();
            int fullSizeMessage = buffer.getInt();
            byte[] tempBytes = new byte[fullSizeMessage];
            buffer.rewind();
            buffer.get(tempBytes);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //[int,byte,byte,short,text]
    public Message decode(ByteBuffer buffer) {
        buffer.flip();
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
