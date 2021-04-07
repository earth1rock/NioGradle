package codec;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Reader {
    private final SocketChannel socketChannel;
    //2 bytes for message length
    private final static int HEADER_SIZE = 2;

    public Reader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String readMessage() throws Exception {
        try {
            ByteBuffer messageLengthBuffer = ByteBuffer.allocate(HEADER_SIZE);
            socketChannel.read(messageLengthBuffer);
            short messageLength = messageLengthBuffer.rewind().getShort();
            ByteBuffer message = ByteBuffer.allocate(HEADER_SIZE + messageLength);
            message.put(messageLengthBuffer.rewind());
            socketChannel.read(message);
            message.rewind();
            return Codec.decode(message);
        } catch (Exception e) {
            socketChannel.close();
            throw new Exception(e);
        }
    }

}
