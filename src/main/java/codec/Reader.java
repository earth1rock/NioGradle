package codec;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Reader {
    public static final byte MAX_MESSAGE_LENGTH_BYTE = 4;
    private final SocketChannel socketChannel;

    public Reader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int getLengthMessage() {
        ByteBuffer messageSize = ByteBuffer.allocate(MAX_MESSAGE_LENGTH_BYTE);
        try {
            if (socketChannel.read(messageSize) > -1) {
                messageSize.flip();
                byte[] bytes = new byte[messageSize.limit()];
                messageSize.get(bytes);
                messageSize.clear();
                return Integer.parseInt(new String(bytes));
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    public String getMessageText(int messageLength) throws Exception {
        if (messageLength > 0) {
            ByteBuffer messageBuffer = ByteBuffer.allocate(messageLength);
            try {
                if (socketChannel.read(messageBuffer) > -1) {
                    messageBuffer.flip();
                    byte[] message = new byte[messageBuffer.limit()];
                    messageBuffer.get(message);
                    messageBuffer.clear();
                    return new String(message);
                }
            } catch (Exception e) {
                throw new Exception("Error reading message");
            }
        }
        return "";
    }

}
