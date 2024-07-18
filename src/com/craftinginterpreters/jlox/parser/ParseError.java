package com.craftinginterpreters.jlox.parser;

public class ParseError extends RuntimeException {

    ParseError(String message) {
        super(message);
    }

    ParseError() {
        super();
    }
}
