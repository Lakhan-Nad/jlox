package com.craftinginterpreters.jlox.parser;

import java.util.List;
import java.util.ArrayList;

import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;
import com.craftinginterpreters.jlox.tools.ErrorHandler;

/**
 * 
 * statement → printStatement
 *           | varDeclaration
 *           | expression
 *           | blockStatement ;
 * varDeclaration → "var" identifierDeclaration ( "," identifierDeclaration) ;
 * identifierDeclaration → IDENTIFIER ("=" expression)? ;
 * expression → assignment ( "," expression )* ;
 * assignment → IDENTIFIER "=" expression
 *            | equality  ;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary
 *       | primary ;
 * primary → NUMBER | STRING | "true" | "false" | "nil"
 *         | "(" expression ")"
 *         | IDENTIFIER ;
 */

public class Parser {
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    public List<Statement> parse() {
        try {
            List<Statement> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(statement());
            }
            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    private Statement statement() {
        try {
            if (match(TokenType.LEFT_BRACE)) return block();
            if (match(TokenType.VAR)) return varDeclaration();
            if (match(TokenType.PRINT)) return printStatement();
            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
       }
    }

    private Statement block() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
          statements.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return new Statement.Block(statements);
    }

    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement expressionStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of statement");
        return new Statement.Expr(expr);
    }

    private Statement printStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of print statement");
        return new Statement.Print(expr);
    }

    private Expression expression() {
        List<Expression> expressions = new ArrayList<Expression>();
        Expression expr = assignment();
        while (match(TokenType.COMMA)) {
            expressions.add(assignment());
        }
        if (expressions.size() == 0) {
            return expr;
        }
        expressions.add(0, expr);
        return new Expression.CommaSeperated(expressions);
    }

    private Expression assignment() {
        Expression expr = equality();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();
            if (expr instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expr).name;
                return new Expression.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
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
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.STAR_STAR)) {
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
        if (match(TokenType.FALSE))
            return new Expression.Literal(false);
        if (match(TokenType.TRUE))
            return new Expression.Literal(true);
        if (match(TokenType.NIL))
            return new Expression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
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
            if (previous().type == TokenType.SEMICOLON)
                return;

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
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        ParseError err = new ParseError(token, message);
        ErrorHandler.parseError(err);
        return err;
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
