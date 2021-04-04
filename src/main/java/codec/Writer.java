package codec;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Writer {
    private final SocketChannel socketChannel;
    private final int MAX_SIZE = 10000;

    public Writer(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int sendMessage(String message) throws Exception {
        if (message.length() < MAX_SIZE) {
            try {
                message = encodeMessage(message);
                ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
                return socketChannel.write(messageBuffer);
            } catch (Exception e) {
                throw new Exception("Error sending message");
            }
        }
        else {
            throw new Exception("Max message size = " + MAX_SIZE + " | Your message size is " + message.length());
        }
    }

    public static String encodeMessage(String message) {
        return String.format("%04d", message.length()) + message;
    }

}
