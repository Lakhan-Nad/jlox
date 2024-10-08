package com.craftinginterpreters.jlox.syntax;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public int slot;
    public int hops;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

    public void attachEnvData(int slot, int hops) {
        this.slot = slot;
        this.hops = hops;
    }
}