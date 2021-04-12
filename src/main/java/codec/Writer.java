package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Writer {
    private final SocketChannel socketChannel;
    private final Codec codec;

    public Writer(SocketChannel socketChannel, Codec codec) {
        this.socketChannel = socketChannel;
        this.codec = codec;
    }

    public int writeMessage(Message message) throws Exception {
        ByteBuffer buffer = codec.encode(message);
        buffer.flip();
        return socketChannel.write(buffer);
    }

}
