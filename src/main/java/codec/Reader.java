package codec;

import message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Reader {
    private final SocketChannel socketChannel;
    private int buffer_size = 10;
    private final int buffer_size_step = 10;
    private final Codec codec;

    public Reader(SocketChannel socketChannel, Codec codec) {
        this.socketChannel = socketChannel;
        this.codec = codec;
    }

    public Message readMessage() throws Exception {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(buffer_size);
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
            while (!codec.canDecode(byteBuffer)) {
                byteBuffer.rewind();
                ByteBuffer temp = byteBuffer.duplicate();
                buffer_size += buffer_size_step;
                byteBuffer = ByteBuffer.allocate(buffer_size);
                byteBuffer.put(temp);
                socketChannel.read(byteBuffer);
            }
            buffer_size = buffer_size_step;
            return codec.decode(byteBuffer);

        } catch (Exception e) {
            socketChannel.close();
            throw e;
        }
    }

}
