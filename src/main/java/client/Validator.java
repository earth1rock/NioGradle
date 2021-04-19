package client;

import server.Viewer;

public class Validator {

    private static final byte MAX_LENGTH = Byte.MAX_VALUE;
    private final Viewer viewer;

    public Validator(Viewer viewer) {
        this.viewer = viewer;
    }

    public boolean validateNickname(String name) {
        if (name.length() > 6 && name.length() <= MAX_LENGTH) {
            return true;
        } else {
            viewer.print("Nickname length must be in [7;" + MAX_LENGTH + "]");
            return false;
        }
    }

    public boolean validateRoomName(String name) {
        return name != null && name.length() > 6 && name.length() <= MAX_LENGTH;
    }

    public boolean validateCommand(String[] commands) {
        if (commands.length != 2) {
            return false;
        }
        String nameOfRoom = commands[1];
        return validateRoomName(nameOfRoom);
    }
}
