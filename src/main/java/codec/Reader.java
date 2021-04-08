package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Reader {
    private final SocketChannel socketChannel;
    //4 bytes for message length
    private final static int HEADER_SIZE = 4;

    public Reader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public Message readMessage() throws Exception {
        try {
            ByteBuffer fullSizeMessageBuffer = ByteBuffer.allocate(HEADER_SIZE);
            socketChannel.read(fullSizeMessageBuffer);
            int fullSizeMessageLength = fullSizeMessageBuffer.rewind().getInt();
            ByteBuffer message = ByteBuffer.allocate(fullSizeMessageLength);
            message.put(fullSizeMessageBuffer.rewind());
            socketChannel.read(message);
            message.rewind();
            return Codec.decode(message);
        } catch (Exception e) {
            socketChannel.close();
            throw new Exception(e);
        }
    }

}
