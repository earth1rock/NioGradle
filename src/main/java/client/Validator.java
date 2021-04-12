package client;

import server.Viewer;

public class Validator {

    private static final byte MAX_LENGTH = Byte.MAX_VALUE;
    private final static Viewer viewer = new Viewer();

    public boolean validate(String name) {
        if (name.length() > 6 && name.length() <= MAX_LENGTH) {
            return true;
        } else {
            viewer.print("Nickname length must be in [7;" + MAX_LENGTH + "]");
            return false;
        }
    }
}
