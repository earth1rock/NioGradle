package codec;

import message.Message;

import java.nio.ByteBuffer;

public class Codec {

    //fullMessageLength (int) -> 4 bytes
    private static final byte FULL_LENGTH_BYTES = 4;

    //fullMessageLength(4) + typeMessage(1) + nameLength(1) + messageLength(2)
    private static final byte HEADER_SIZE = 8;

    //why is this limit. On lengthMessage == MAX_VALUE. App is crash
    private static final short MAX_LENGTH = Short.MAX_VALUE;

    //rest bytes
    private ByteBuffer restBuffer;
    private int temp = 0;

    public ByteBuffer encode(Message message) throws Exception {

        if (message.getMessage().length() <= MAX_LENGTH) {
            byte messageType = message.getMessageType();
            byte userNameLength = (byte) message.getUserName().length();
            short messageLength = (short) message.getMessage().length();

            int fullSizeBufferLength = HEADER_SIZE +
                    message.getUserName().length() +
                    message.getMessage().length();
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

    public boolean canDecode(ByteBuffer buffer) {
        int capacity;
        int messageLength;
        buffer.rewind();

        if (temp == 0) {
            messageLength = buffer.getInt();
            capacity = buffer.capacity();
        } else if (temp >= HEADER_SIZE) {
            restBuffer.flip();
            messageLength = canReadLength(restBuffer) ? restBuffer.rewind().getInt() : buffer.rewind().getInt();
            capacity = restBuffer.capacity() + buffer.capacity();
        } else {
            ByteBuffer tempBuffer = concatBuffsForMsgLength(buffer);
            messageLength = canReadLength(tempBuffer) ? tempBuffer.rewind().getInt() : buffer.rewind().getInt();
            capacity = restBuffer.capacity() + buffer.capacity();
        }

        return messageLength <= capacity;

    }

    private boolean canReadLength(ByteBuffer mainBuffer) {
        try {
            if (mainBuffer.rewind().getInt() == 0) {
                temp = 0;
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ByteBuffer concatBuffsForMsgLength(ByteBuffer inputBuffer) {
        ByteBuffer tempMessageLength = ByteBuffer.allocate(HEADER_SIZE);
        byte[] bytes = new byte[HEADER_SIZE];
        restBuffer.rewind().get(bytes, 0, temp);
        inputBuffer.get(bytes, temp, HEADER_SIZE - temp);
        tempMessageLength.put(bytes).rewind();
        return tempMessageLength;
    }

    public Message decode(ByteBuffer buffer) throws Exception {
        buffer.rewind();
        int fullSizeMessage;
        byte messageType;
        byte userNameLength;
        short messageLength;
        int resBufferSize;

        if (temp == 0) {
            fullSizeMessage = buffer.getInt();
            messageType = buffer.get();
            userNameLength = buffer.get();
            messageLength = buffer.getShort();

            byte[] userNameLengthBuffer = new byte[userNameLength];
            buffer.get(userNameLengthBuffer);
            String userName = new String(userNameLengthBuffer);

            byte[] messageLengthBuffer = new byte[messageLength];
            buffer.get(messageLengthBuffer);
            String messageString = new String(messageLengthBuffer);

            Message message = new Message(messageType, userName, messageString);

            temp = buffer.remaining();

            if (temp != 0) {
                restBuffer = ByteBuffer.allocate(temp);
                restBuffer.put(buffer);
                canReadLength(restBuffer);
            }
            return message;
        } else {

            if (temp < FULL_LENGTH_BYTES) {
                fullSizeMessage = concatBuffsForMsgLength(buffer).getInt();
            } else {
                fullSizeMessage = restBuffer.rewind().getInt();
            }
            resBufferSize = restBuffer.capacity();

            byte[] message = new byte[fullSizeMessage];
            restBuffer.clear().get(message, 0, resBufferSize);
            int secondHalfMessage = fullSizeMessage - resBufferSize;
            buffer.clear().get(message, resBufferSize, secondHalfMessage);

            ByteBuffer resultMessageObj = ByteBuffer.allocate(fullSizeMessage);
            resultMessageObj.put(message);

            resultMessageObj.rewind();

            fullSizeMessage = resultMessageObj.getInt();
            messageType = resultMessageObj.get();
            userNameLength = resultMessageObj.get();
            messageLength = resultMessageObj.getShort();

            byte[] userNameLengthBuffer = new byte[userNameLength];
            resultMessageObj.get(userNameLengthBuffer);
            String userName = new String(userNameLengthBuffer);

            byte[] messageLengthBuffer = new byte[messageLength];
            resultMessageObj.get(messageLengthBuffer);
            String messageString = new String(messageLengthBuffer);

            Message messageObj = new Message(messageType, userName, messageString);

            temp = buffer.capacity() - buffer.position();
            restBuffer = ByteBuffer.allocate(temp);
            restBuffer.put(buffer);

            canReadLength(restBuffer);

            return messageObj;
        }
    }
}
