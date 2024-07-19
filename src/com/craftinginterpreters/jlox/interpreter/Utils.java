package com.craftinginterpreters.jlox.interpreter;

public class Utils {
    public static double compareStrings(String left, String right) {
        int minLen = Math.min(left.length(), right.length());
        int diff;
        for (int i = 0; i < minLen; i++) {
            diff = left.codePointAt(i) - right.codePointAt(i);
            if (diff == 0) {
                continue;
            }
            return diff;
        }
        if (left.length() == right.length()) {
            return 0;
        }
        return left.length() - right.length();
    }
}
