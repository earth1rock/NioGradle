package codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Reader {
    private final SocketChannel socketChannel;
    private int buffer_size = 10;
    private int buffer_size_step = 10;

    public Reader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String readMessage() throws Exception {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(buffer_size);
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
            while (!Codec.canDecode(byteBuffer)) {
                ByteBuffer temp = byteBuffer.duplicate();
                buffer_size += buffer_size_step;
                byteBuffer = ByteBuffer.allocate(buffer_size);
                byteBuffer.put(temp);
                socketChannel.read(byteBuffer);
                byteBuffer.rewind();
            }
            byteBuffer.rewind();
            return Codec.decode(byteBuffer);

        } catch (Exception e) {
            socketChannel.close();
            throw new Exception(e);
        }
    }

}
