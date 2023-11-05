package de.laurinhummel.SparkSRV.handler;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkyLogger {
    private static final Logger logger = Logger.getLogger("de.laurinhummel");

    public static void log(Level level, String message) {
        logger.log(level, message);
    }
    public static void log(String message) {
        logger.log(Level.INFO, message);
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
