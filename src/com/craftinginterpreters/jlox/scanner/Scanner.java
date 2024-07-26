package com.craftinginterpreters.jlox.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftinginterpreters.jlox.Constants;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;

public class Scanner {
    private int current;
    private int start;
    private int line;
    private String source;

    private List<Token> tokens;

    public Scanner(String source) {
        this.source = source;
        this.current = 0;
        this.start = 0;
        this.line = 1;
        this.tokens = new ArrayList<Token>();
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
          this.start = this.current;
          scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
      }

    public void scanToken() {
        char c = peek();
        if (Character.isAlphabetic(c)) {
            consumeLiteral();
            return;
        } else if (Character.isDigit(c)) {
            consumeNumber();
            return;
        }
        switch (c) {
            case '(': {
                advance();
                addToken(TokenType.LEFT_PAREN);
                break;
            }
            case ')': {
                advance();
                addToken(TokenType.RIGHT_PAREN);
                break;
            }
            case '{': {
                advance();
                addToken(TokenType.LEFT_BRACE);
                break;
            }
            case '}': {
                advance();
                addToken(TokenType.RIGHT_BRACE);
                break;
            }
            case '+': {
                advance();
                addToken(TokenType.PLUS);
                break;
            }
            case '-': {
                advance();
                addToken(TokenType.MINUS);
                break;
            }
            case ',': {
                advance();
                addToken(TokenType.COMMA);
                break;
            }
            case ';': {
                advance();
                addToken(TokenType.SEMICOLON);
                break;
            }
            case '.': {
                advance();
                addToken(TokenType.DOT);
                break;
            }
            case '*': {
                advance();
                if (peek() == '*') {
                    advance();
                    addToken(TokenType.STAR_STAR);
                } else {
                    addToken(TokenType.STAR);
                }
                break;
            }
            case '/': {
                char nextPeek = peek(1);
                if (nextPeek == '*') {
                    consumeMultiLineComment();
                } else if (nextPeek == '/') {
                    consumeSingleLineComment();
                } else {
                    advance();
                    addToken(TokenType.SLASH);
                }
                break;
            }
            case '=': {
                char nextPeek = peek(1);
                if (nextPeek == '=') {
                    advance(1);
                    addToken(TokenType.EQUAL_EQUAL);
                } else {
                    advance();
                    addToken(TokenType.EQUAL);
                }
                break;
            }
            case '>': {
                char nextPeek = peek(1);
                if (nextPeek == '=') {
                    advance(1);
                    addToken(TokenType.GREATER_EQUAL);
                } else {
                    advance();
                    addToken(TokenType.GREATER);
                }
                break;
            }
            case '<': {
                char nextPeek = peek(1);
                if (nextPeek == '=') {
                    advance(1);
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    advance();
                    addToken(TokenType.LESS);
                }
                break;
            }
            case '!': {
                char nextPeek = peek(1);
                if (nextPeek == '=') {
                    advance(1);
                    addToken(TokenType.BANG_EQUAL);
                } else {
                    advance();
                    addToken(TokenType.BANG);
                }
                break;
            }
            case '"': 
            case '\'': {
                consumeString();
                break;
            }
            case ' ':
            case '\r':
            case '\t': {
                advance();
                break;
            }
            case '\n': {
                this.line += 1;
                advance();
                break;
            }
            case '&': {
                if (peek(1) == '&') {
                    advance(1);
                    addToken(TokenType.AND);
                } else {
                    advance();
                    addToken(TokenType.AMPERSAND);
                }
                break;
            }
            case '|': {
                if (peek(1) == '|') {
                    advance(1);
                    addToken(TokenType.OR);
                } else {
                    advance();
                    addToken(TokenType.SINGLE_OR);
                }
                break;
            }
            default: {
                advance();
                ErrorHandler.error(this.line, String.format("unknown character encountered %c", c));
            }
        }
    }

    // type consumers
    private void consumeNumber() {
        while (Character.isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && Character.isDigit(peek(1))) {
            advance();
            while (Character.isDigit(peek())) {
                advance();
            }
        }
        addToken(TokenType.NUMBER, Double.parseDouble(getCurrentLiteral()));
    }

    private void consumeLiteral() {
        if (Character.isAlphabetic(peek())) {
            advance();
            char c = peek();
            while (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                advance();
                c = peek();
            }
            addToken(TokenType.IDENTIFIER, getCurrentLiteral());
            return;
        }
        advance();
        ErrorHandler.error(this.line, "error in literal parsing");
    }

    private void consumeString() {
        char startQuote = advance(); // consume "/'
        StringBuffer buf = new StringBuffer();
        while (!isAtEnd()) {
            char c = peek();
            if (c == startQuote) {
                advance();
                addToken(TokenType.STRING, buf.toString());
                return;
            } else if (c == Constants.EOL_CHAR) {
                advance();
                this.line += 1;
                ErrorHandler.error(this.line, "unclosed string found");
                return;
            }
            char ch = consumeCharacter(c);
            buf.append(ch);
        }
        ErrorHandler.error(this.line, "un closed string found");
    }

    private char consumeCharacter(char c) {
        if (c == '\\') {
            advance();
            char next = advance();
            switch (next) {
                case '\\':
                    return '\\';
                case 't':
                    return '\t';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 'b':
                    return '\b';
                case '\'':
                    return '\'';
                case '\"':
                    return '\"';
                default:
                    ErrorHandler.error(this.line, String.format("unknown special character %c%c", "\\", next));
                    return Constants.NULL_CHAR;
            }
        }
        advance();
        return c;
    }

    private void consumeMultiLineComment() {
        advance(1); /* consume /* */
        while (!isAtEnd()) {
            char c = peek();
            if (c == '*' && peek(1) == '/') {
                advance(1);
                addToken(TokenType.MULTI_LINE_COMMENT, getCurrentLiteral());
                return;
            } else {
                if (c == Constants.EOL_CHAR) {
                    this.line += 1;
                }
                advance();
            }
        }
        ErrorHandler.error(line, "un closed multi line comment found");
    }

    private void consumeSingleLineComment() {
        advance(1); // consume //
        while (!isAtEnd()) {
            char c = peek();
            if (c == Constants.EOL_CHAR) {
                advance();
                addToken(TokenType.SINGLE_LINE_COMMENT, getCurrentLiteral());
                this.line += 1;
                return;
            } else {
                advance();
            }
        }
        ErrorHandler.error(line, "un closed single line comment (/*) found");
    }

    // helpers

    private String getCurrentLiteral() {
        return this.source.substring(this.start, this.current);
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private char peek() {
        if (isAtEnd())
            return Constants.NULL_CHAR;
        return this.source.charAt(current);
    }

    private char peek(int forward) {
        if (forward < 0)
            return Constants.NULL_CHAR;
        if (this.current + forward < this.source.length())
            return this.source.charAt(this.current + forward);
        return Constants.NULL_CHAR;
    }

    private char advance() {
        if (isAtEnd())
            return Constants.NULL_CHAR;
        return this.source.charAt(this.current++);
    }

    private char advance(int forward) {
        if (forward < 0)
            return Constants.NULL_CHAR;
        if (this.current + forward < this.source.length()) {
            this.current += forward;
            return this.source.charAt(this.current++);
        }
        return Constants.NULL_CHAR;
    }

    // token functions
    void addToken(TokenType type) {
        addToken(type, null);
    }

    void addToken(TokenType type, Object literal) {
        Logger.trace(String.format("scanner:add_token <line : %d> %s %s", this.line, type.name(), (literal == null ? "<null>" : literal.toString())));
        if (type == TokenType.IDENTIFIER) {
            type = Scanner.keywords.getOrDefault(literal, TokenType.IDENTIFIER);
        }
        String text = getCurrentLiteral();
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    // keywords
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
    }
}