package session;

import client.User;
import codec.Codec;
import message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public class Session {

    private int buffer_size = 10;
    private final int buffer_size_step = 10;
    private final SocketChannel socketChannel;

    private User user;
    private final Codec codec;
    private ByteBuffer restBuffer;


    public Session(SocketChannel socketChannel, Codec codec) {
        this(socketChannel, codec, null);
    }

    public Session(SocketChannel socketChannel, Codec codec, User user) {
        this.socketChannel = Objects.requireNonNull(socketChannel, "SocketChannel must not be null");
        this.codec = Objects.requireNonNull(codec, "Codec must not be null");
        this.user = user;
    }

    public void attachUser(Object object) {
        this.user = (User) object;
    }

    public User getUser() {
        return user;
    }

    private ByteBuffer getRest(ByteBuffer buffer) {
        int oldLimPos = buffer.limit();
        buffer.limit(buffer.capacity());
        int restBytes = buffer.remaining();
        if (restBytes != 0) {
            ByteBuffer restBuffer = ByteBuffer.allocate(restBytes);
            restBuffer.put(buffer);
            buffer.limit(oldLimPos);
            restBuffer.flip();
            return restBuffer;
        }
        return null;
    }

    public Message readMessage() throws Exception {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(buffer_size);
            if (restBuffer != null) {
                byteBuffer.put(restBuffer);
            }

            int bytesRead = socketChannel.read(byteBuffer);
            checkBytesRead(bytesRead);

            while (!codec.canDecode(byteBuffer)) {
                ByteBuffer temp = byteBuffer.duplicate();
                buffer_size += buffer_size_step;
                byteBuffer = ByteBuffer.allocate(buffer_size);
                byteBuffer.put(temp);
                bytesRead = socketChannel.read(byteBuffer);
                checkBytesRead(bytesRead);
            }

            restBuffer = (bytesRead == buffer_size_step) ? getRest(byteBuffer) : null;
            buffer_size = buffer_size_step;
            byteBuffer.flip();
            return codec.decode(byteBuffer);

        } catch (Exception e) {
            socketChannel.close();
            throw e;
        }
    }

    private void checkBytesRead(int bytesRead) throws Exception {
        if (bytesRead == -1) {
            socketChannel.close();
            throw new Exception("Channel has reached end-of-stream");
        }
    }

    public int writeMessage(Message message) throws Exception {
        ByteBuffer buffer = codec.encode(message);
        buffer.flip();
        return socketChannel.write(buffer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(socketChannel, session.socketChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketChannel);
    }

    public void close() throws IOException {
        socketChannel.close();
    }
}
