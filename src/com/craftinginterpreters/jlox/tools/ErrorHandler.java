package com.craftinginterpreters.jlox.tools;

import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;

public class ErrorHandler {
    public static boolean hadError;

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
