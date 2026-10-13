package common;

public final class Protocol {

    private Protocol() {}

    public static final String JOIN = "JOIN";
    public static final String MSG  = "MSG";
    public static final String EXIT = "EXIT";

    public static String formatJoin(String username) {
        return JOIN + ":" + username;
    }

    public static String formatMessage(String username, String content) {
        return MSG + ":" + username + ":" + content;
    }

    public static String formatExit(String username) {
        return EXIT + ":" + username;
    }
}
