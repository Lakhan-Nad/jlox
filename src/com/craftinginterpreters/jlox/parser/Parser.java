package com.craftinginterpreters.jlox.parser;

import java.util.List;

import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;
import com.craftinginterpreters.jlox.tools.ErrorHandler;

public class Parser {
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    public Expression parse() {
        try {
            return expression();
        } catch(ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token op = previous();
            Expression right = comparison();
            expr = new Expression.Binary(expr, op, right);
        }
        return expr;
    }

    private Expression comparison() {
        Expression expr = term();
        while (match(TokenType.GREATER_EQUAL, TokenType.GREATER, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token op = previous();
            Expression right = term();
            expr = new Expression.Binary(expr, op, right);
        }
        return expr;
    }

    private Expression term() {
        Expression expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token op = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, op, right);
        }
        return expr;
    }

    private Expression factor() {
        Expression expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.POWER)) {
            Token op = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, op, right);
        }
        return expr;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token op = previous();
            Expression expr = unary();
            return new Expression.Unary(op, expr);
        }
        return primary();
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) return new Expression.Literal(false);
        if (match(TokenType.TRUE)) return new Expression.Literal(true);
        if (match(TokenType.NIL)) return new Expression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();
    
        while (!isAtEnd()) {
          if (previous().type == TokenType.SEMICOLON) return;
    
          switch (peek().type) {
            case CLASS:
            case FUN:
            case VAR:
            case FOR:
            case IF:
            case WHILE:
            case PRINT:
            case RETURN:
              return;
            default:
                break;
          }
    
          advance();
        }
      }

    // helpers

    private Token consume(TokenType type, String message) throws ParseError {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        ErrorHandler.error(token, message);
        return new ParseError(message);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }
}
