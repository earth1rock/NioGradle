package client;

import server.Viewer;

import java.util.Objects;

public class Validator {

    private static final byte MAX_LENGTH = Byte.MAX_VALUE;
    private final Viewer viewer;

    public Validator(Viewer viewer) {
        this.viewer = Objects.requireNonNull(viewer, "Viewer must not be null");
    }

    public boolean validateNickname(String name) {
        if (name.length() > 6 && name.length() <= MAX_LENGTH) {
            return true;
        } else {
            viewer.print("Nickname length must be in [7;" + MAX_LENGTH + "]");
            return false;
        }
    }

    public boolean validateRoomName(String nameOfRoom) {
        return nameOfRoom != null && nameOfRoom.length() > 6 && nameOfRoom.length() <= MAX_LENGTH;
    }
}
