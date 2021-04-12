package message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageFormatter {
    public static String formatMessage(Message message) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss | ");
        return dateFormat.format(date) + message.getUserName() + ": " + message.getMessage();
    }

    public static String formatForLogs(Message message) {
        return message.getUserName() + ": " + message.getMessage();
    }
}
