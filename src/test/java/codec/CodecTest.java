package codec;

import message.Message;
import message.MessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CodecTest {

    private static Message tempMessage;
    private static Codec codec;
    private static ByteBuffer expected;

    @BeforeAll
    static void init() {
        tempMessage = new Message(MessageType.MESSAGE, "user", "hello");
        codec = new Codec();

        byte[] userNameBytes = tempMessage.getUserName().getBytes(StandardCharsets.UTF_8);
        byte[] messageBytes = tempMessage.getMessage().getBytes(StandardCharsets.UTF_8);

        byte messageType = tempMessage.getMessageType();
        byte userNameLength = (byte) userNameBytes.length;
        short messageLength = (short) messageBytes.length;

        int fullSizeMessage = 8 + userNameLength + messageLength;

        expected = ByteBuffer.allocate(fullSizeMessage);

        //[int,byte,byte,short,text]
        expected.putInt(fullSizeMessage);
        expected.put(messageType);
        expected.put(userNameLength);
        expected.putShort(messageLength);
        expected.put(userNameBytes);
        expected.put(messageBytes);
    }

    @Test
    void encode_tempMessage_test() {
        ByteBuffer actual = codec.encode(tempMessage);
        expected.flip();
        actual.flip();
        assertEquals(expected, actual);

    }
}