package com.craftinginterpreters.jlox.tools;

import com.craftinginterpreters.jlox.interpreter.RuntimeError;
import com.craftinginterpreters.jlox.parser.ParseError;
import com.craftinginterpreters.jlox.syntax.TokenType;

public class ErrorHandler {
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    public static void resetErrors() {
        ErrorHandler.hadError = false;
        ErrorHandler.hadRuntimeError = false;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void parseError(ParseError error) {
        if (error.token.type == TokenType.EOF) {
            report(error.token.line, " at end", error.getMessage());
        } else {
            report(error.token.line, " at '" + error.token.lexeme + "'", error.getMessage());
        }
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[line " + error.token.line + "] Error: " + error.getMessage() + " near token " + error.token.lexeme);
        hadRuntimeError = true;
    }
}
