package de.laurinhummel.SparkSRV.handler;

import java.util.Calendar;

public class SkyLogger {
    public enum Level {
        ANSI_RESET("\u001B[0m"),
        ANSI_BLACK("\u001B[30m"),
        ANSI_RED("\u001B[31m"),
        ANSI_GREEN("\u001B[32m"),
        ANSI_YELLOW("\u001B[33m"),
        ANSI_BLUE("\u001B[34m"),
        ANSI_PURPLE("\u001B[35m"),
        INFO("\u001B[36m"),  //Cyan
        ANSI_WHITE("\u001B[37m");

        private final String level;
        Level(String level) { this.level = level; }
        public String toString() { return this.level; }
    }

    public static void log(Level level, String message) {
        System.out.println(Level.ANSI_YELLOW.toString() + Calendar.getInstance().getTime() + Level.ANSI_RESET + " - " + level.toString() + message + Level.ANSI_RESET);
    }

    public static void log(String message) {
        System.out.println(Level.ANSI_YELLOW.toString() + Calendar.getInstance().getTime() + Level.ANSI_RESET + " - " + Level.INFO + message);
    }

    public static void logStack(Throwable ex) {
        ex.printStackTrace();
        /*
        String message = ex.getMessage();
        String msg = ex.toString();
        Throwable stack = ex.fillInStackTrace();

        logger.log(Level.WARNING, message + "\n" + msg + "\n" + stack);
        */

    }
}
