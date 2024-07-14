package com.craftinginterpreters.jlox.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static class Level {
        public static int TRACE = 0;
        public static int DEBUG = 1;
        public static int INFO = 2;
        public static int WARN = 3;
        public static int ERROR = 4;
    };

    private static String time() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static void error(String formattedString) {
        print("ERROR", formattedString);
    }
    public static void warn(String formattedString) {
        print("WARN", formattedString);
    }
    public static void info(String formattedString) {
        print("INFO", formattedString);
    }
    public static void debug(String formattedString) {
        print("DEBUG", formattedString);
    }
    public static void trace(String formattedString) {
        print("TRACE", formattedString);
    }

    private static void print(String type, String formattedString) {
        System.out.println(String.format("[%s : %s] %s", type, time(), formattedString));
    }
}