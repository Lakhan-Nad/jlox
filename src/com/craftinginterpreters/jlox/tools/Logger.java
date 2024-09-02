package com.craftinginterpreters.jlox.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static int level;
    public static class Level {
        public static final int TRACE = 0;
        public static final int DEBUG = 1;
        public static final int INFO = 2;
        public static final int WARN = 3;
        public static final int ERROR = 4;
    };

    public static void setLogLevel(int level) {
        Logger.level = Math.min(Level.ERROR, Math.max(0, level));
    }

    public static void error(String formattedString) {
        if (Logger.level <= Level.ERROR) {
            print("ERROR", formattedString);
        }
    }
    public static void warn(String formattedString) {
        if (Logger.level <= Level.WARN) {
            print("WARN", formattedString);
        }
    }
    public static void info(String formattedString) {
        if (Logger.level <= Level.INFO) {
            print("INFO", formattedString);
        }
    }
    public static void debug(String formattedString) {
        if (Logger.level <= Level.DEBUG) {
            print("DEBUG", formattedString);
        }
    }
    public static void trace(String formattedString) {
        if (Logger.level <= Level.TRACE) {
            print("TRACE", formattedString);
        }
    }

    private static void print(String type, String formattedString) {
        System.out.println(String.format("[%s : %s] %s", type, time(), formattedString));
    }

    private static String time() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}