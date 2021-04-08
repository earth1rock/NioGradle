package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Writer {
    private final SocketChannel socketChannel;

    public Writer(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int writeMessage(Message message) throws Exception {
        ByteBuffer buffer = Codec.encode(message);
        buffer.flip();
        return socketChannel.write(buffer);
    }

}
