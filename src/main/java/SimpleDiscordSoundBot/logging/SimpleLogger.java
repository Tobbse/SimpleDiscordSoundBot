package SimpleDiscordSoundBot.logging;

public abstract class SimpleLogger {
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";

    public static void info(String message) {
        System.out.println(message);
    }

    public static void warn(String message) {
        System.err.println(YELLOW + message + YELLOW);
    }

    public static void logException(Exception e, String message) {
        if (message != null) System.err.println(RED + message + RED);
        if (e != null) e.printStackTrace();
    }
}