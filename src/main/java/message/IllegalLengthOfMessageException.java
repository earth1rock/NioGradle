package message;

public class IllegalLengthOfMessageException extends IllegalArgumentException {
    public IllegalLengthOfMessageException(int maxSize, int currentSize) {
        super("Max message size = " + maxSize + " | Your message size is " + currentSize);
    }
}
