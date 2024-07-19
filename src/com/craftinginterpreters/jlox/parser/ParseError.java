package com.craftinginterpreters.jlox.parser;

import com.craftinginterpreters.jlox.syntax.Token;

public class ParseError extends RuntimeException {
    public final Token token;

    ParseError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
