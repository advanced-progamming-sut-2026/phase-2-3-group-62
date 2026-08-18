package util;

public class Logger {
    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}

