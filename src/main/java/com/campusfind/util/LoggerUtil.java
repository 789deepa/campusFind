package com.campusfind.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Configures application-wide logging using standard java.util.logging.
 * Ensures concise, readable log output and reminds developers never to log confidential clues.
 */
public final class LoggerUtil {

    private static boolean configured = false;

    private LoggerUtil() {}

    public static synchronized void configureLogging(Level rootLevel) {
        if (configured) return;

        Logger rootLogger = Logger.getLogger("");
        // Remove existing handlers to avoid duplicate output
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(rootLevel);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%1$tF %1$tT] [%2$-7s] %3$s%n",
                        record.getMillis(),
                        record.getLevel().getName(),
                        record.getMessage());
            }
        });

        rootLogger.addHandler(handler);
        rootLogger.setLevel(rootLevel);
        configured = true;
    }

    public static Logger getLogger(Class<?> clazz) {
        if (!configured) {
            configureLogging(Level.INFO);
        }
        return Logger.getLogger(clazz.getName());
    }
}
