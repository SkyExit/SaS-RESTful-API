package de.laurinhummel.SparkSRV.handler;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkyLogger {
    private static final Logger logger = Logger.getLogger("de.laurinhummel");

    public static void log(Level level, String message) {
        logger.log(level, message);
    }

    public static void logStack(Throwable ex) {
        String message = ex.getMessage();
        StackTraceElement[] stack = ex.getStackTrace();

        logger.log(Level.WARNING, message + "\n" + Arrays.toString(stack));
    }
}
