package session;

import codec.Codec;
import message.Message;
import message.MessageType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

class SessionTest {

    private static final Codec codec = mock(Codec.class);
    private static final SocketChannel socketChannel = mock(SocketChannel.class);
    private static final Session session = new Session(socketChannel, codec);
    private static final Message tempMessage = new Message(MessageType.MESSAGE, "user", "hello");


    @Test
    void writeMessage() throws Exception {
        byte[] bytes = tempMessage.getMessage().getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);

        ByteBuffer flippedBuffer = byteBuffer.duplicate().flip();

        when(codec.encode(tempMessage)).thenReturn(byteBuffer.flip());
        when(socketChannel.write(flippedBuffer)).thenReturn(bytes.length);

        session.writeMessage(tempMessage);

        verify(codec, times(1)).encode(tempMessage);
        verify(socketChannel, times(1)).write(flippedBuffer);

    }
}