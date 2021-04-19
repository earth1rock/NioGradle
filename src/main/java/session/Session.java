package session;

import client.User;
import codec.Codec;
import message.Message;
import room.Room;

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


    public Session(SocketChannel socketChannel, Codec codec) throws Exception {
        this(socketChannel, codec, null);
    }

    public Session(SocketChannel socketChannel, Codec codec, User user) throws NullPointerException{
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

    public Room getRoom() {
        return user.getRoom();
    }

    private ByteBuffer checkerForFullBuffer() {
        try {
            if (restBuffer != null) {
                restBuffer.clear();
                ByteBuffer checker = ByteBuffer.allocate(buffer_size);
                checker.put(restBuffer);
                socketChannel.read(checker);

                try {
                    if (checker.rewind().getInt() == 0) {
                        checker.position(restBuffer.capacity());
                        restBuffer = null;
                    } else {
                        checker.rewind();
                    }
                } catch (Exception e) {
                    checker.position(restBuffer.capacity());
                    restBuffer = null;
                    return checker;
                }
                return checker;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private ByteBuffer getRest(ByteBuffer buffer) {
        int restBytes = buffer.remaining();
        if (restBytes != 0) {
            ByteBuffer restBuffer = ByteBuffer.allocate(restBytes);
            restBuffer.put(buffer);
            return restBuffer;
        }
        return null;
    }

    public Message readMessage() throws Exception {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(buffer_size);
            ByteBuffer tempBuffer = checkerForFullBuffer();
            if (tempBuffer != null) {
                byteBuffer.put(tempBuffer);
            }

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
            restBuffer = getRest(byteBuffer);
            buffer_size = buffer_size_step;
            byteBuffer.flip();
            return codec.decode(byteBuffer);

        } catch (Exception e) {
            socketChannel.close();
            throw e;
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