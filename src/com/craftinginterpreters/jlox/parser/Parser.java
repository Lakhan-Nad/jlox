package com.craftinginterpreters.jlox.parser;

import java.util.List;
import java.util.ArrayList;

import com.craftinginterpreters.jlox.Constants;
import com.craftinginterpreters.jlox.interpreter.FunctionData;
import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;
import com.craftinginterpreters.jlox.tools.ErrorHandler;

/**
 * 
 * statement → printStatement
 *           | varDeclaration
 *           | expressionStatement
 *           | blockStatement
 *           | ifStatement
 *           | whileStatement
 *           | forStatement
 *           | breakStatement
 *           | continueStatament
 *           | functionDeclaration
 *           | returnStatement ;
 * returnStatement → "return" expression? ";" ;
 * functionDeclaration → "fun" function ;
 * function → IDENTIFIER "(" parameters? ")" blockStatement ;
 * parameters → IDENTIFIER ( "," IDENTIFIER );
 * printStatement → "print" expression ";" ;
 * blockStatement → "{" statement* "}" ;
 * whileStatement → "while" "(" commaSeperatedExpression ")" statement ;
 * forStatement → "for" "(" (varDeclaration | expressionStatement | ";" ) commaSeperatedExpression? ";" commaSeperatedExpression? ")" statement ;
 * ifStatement → "if" "(" commaSeperatedExpression ")" statement ( "else" statement )? ;
 * expressionStatement → commaSeperatedExpression ";" ;
 * varDeclaration → "var" IDENTIFIER ( "=" expression )? ";" ;
 * expression → assignment ;
 * commaSeperatedExpression → expression ( "," expression )* ;
 * assignment → IDENTIFIER "=" expression
 *            | logic_or ;
 * logic_or → logic_and ( ("or" | "||") logic_or ) ;
 * logic_and → equality ( ("and" | "&&") logic_and ) ;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary
 *       | call ;
 * call → primary ( "(" arguments? ")" )* ;
 * arguments → expression ( "," expression )
 * primary → NUMBER 
 *         | STRING 
 *         | "true" 
 *         | "false" 
 *         | "nil"
 *         | "(" commaSeperatedExpression ")"
 *         | IDENTIFIER
 *         | "fun" IDENTIFIER? "(" parameters? ")" blockStatement ;
 */

public class Parser {
    private final List<Token> tokens;
    private int current;
    private int withinLoop;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        this.withinLoop = 0;
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
            if (match(TokenType.RETURN))
                return returnStatement();
            if (match(TokenType.FUN))
                return functionStatement();
            if (match(TokenType.WHILE))
                return whileStatament();
            if (match(TokenType.FOR))
                return forStatement();
            if (match(TokenType.BREAK))
                return breakStatement();
            if (match(TokenType.CONTINUE))
                return continueStatement();
            if (match(TokenType.IF))
                return ifStatement();
            if (match(TokenType.LEFT_BRACE))
                return blockStatement();
            if (match(TokenType.VAR))
                return varDeclaration();
            if (match(TokenType.PRINT))
                return printStatement();
            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "expect ; after return value");
        return new Statement.Return(keyword, value);
    }

    private Statement.Function functionStatement() {
        FunctionData data = function("function");
        if (data.name == null) {
            error(previous(), "anonymous function not allowed in function declaration");
        }
        return new Statement.Function(data.name, data.parameters, data.statements);
    }

    private FunctionData function(String kind) {
        Token name = null;
        if (check(TokenType.IDENTIFIER)) {
            name = consume(TokenType.IDENTIFIER, "expect name of a " + kind);
        }
        consume(TokenType.LEFT_PAREN, "expect ( after " + kind + " name");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= Constants.MAX_FUNCTION_PARAMS) {
                    error(peek(),
                            "cannot have more than " + Constants.MAX_FUNCTION_PARAMS + " parameters in a " + kind);
                }
                parameters
                        .add(consume(TokenType.IDENTIFIER, "expect name of an identifier in parameters of a " + kind));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "expect ) after " + kind + " parameters");
        consume(TokenType.LEFT_BRACE, "expect { at start of body of " + kind);
        List<Statement> stmts = getStatements();
        consume(TokenType.RIGHT_BRACE, "expect '}' after " + kind + "statements");
        return new FunctionData(name, parameters, stmts);
    }

    private Statement continueStatement() {
        if (withinLoop == 0) {
            throw error(previous(), "continue cannot be used outside of loops");
        }
        consume(TokenType.SEMICOLON, "; is mandatory after continue");
        return new Statement.Continue();
    }

    private Statement breakStatement() {
        if (withinLoop == 0) {
            throw error(previous(), "break cannot be used outside of loops");
        }
        consume(TokenType.SEMICOLON, "; is mandatory after break");
        return new Statement.Break();
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "missin ( after for");
        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null; // consumed semicolon
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration(); // ends with semicolon, so consumed
        } else {
            initializer = expressionStatement(); // ends with semicolon, so consumed
        }
        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = commaSeperatedExpression();
        }
        consume(TokenType.SEMICOLON, "expect ; after condition in for");
        Expression change = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            change = commaSeperatedExpression();
        }
        consume(TokenType.RIGHT_PAREN, "expect ) after for");
        if (condition == null) {
            condition = new Expression.Literal(true);
        }
        try {
            this.withinLoop++;
            Statement body = statement();
            return new Statement.For(initializer, condition, body, change);
        } finally {
            this.withinLoop--;
        }
    }

    private Statement whileStatament() {
        consume(TokenType.LEFT_PAREN, "missin ( after while");
        Expression condition;
        if (!check(TokenType.RIGHT_PAREN)) {
            condition = commaSeperatedExpression();
        } else {
            condition = new Expression.Literal(true);
        }
        consume(TokenType.RIGHT_PAREN, "missing ) after while and condition");
        try {
            withinLoop++;
            Statement body = statement();
            return new Statement.While(condition, body);
        } finally {
            withinLoop--;
        }
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "missing ( after if");
        Expression ifCondition = commaSeperatedExpression();
        consume(TokenType.RIGHT_PAREN, "missing ) after if and condition");
        Statement thenStatement = statement();
        Statement elseStatement = null;
        if (match(TokenType.ELSE)) {
            elseStatement = statement();
        }
        return new Statement.IfElse(ifCondition, thenStatement, elseStatement);
    }

    private Statement blockStatement() {
        List<Statement> statements = getStatements();
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return new Statement.Block(statements);
    }

    private List<Statement> getStatements() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        return statements;
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
        Expression expr = commaSeperatedExpression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of statement");
        return new Statement.Expr(expr);
    }

    private Statement printStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of print statement");
        return new Statement.Print(expr);
    }

    
    private Expression commaSeperatedExpression() {
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

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expr = or();
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

    private Expression or() {
        Expression expr = and();
        while (match(TokenType.OR)) {
            Token op = previous();
            Expression right = and();
            expr = new Expression.Logical(expr, op, right);
        }
        return expr;
    }

    private Expression and() {
        Expression expr = equality();
        while (match(TokenType.AND)) {
            Token op = previous();
            Expression right = equality();
            expr = new Expression.Logical(expr, op, right);
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
        return call();
    }

    private Expression call() {
        Expression expr = primary();
        while (match(TokenType.LEFT_PAREN)) {
            expr = finishCall(expr);
        }
        return expr;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression());
                if (arguments.size() >= Constants.MAX_FUNCTION_PARAMS) {
                    error(peek(),
                            "cannot have more than " + Constants.MAX_FUNCTION_PARAMS + " arguments in a function");
                }
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "expect ) to end function call");
        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(TokenType.FALSE))
            return new Expression.Literal(false);
        if (match(TokenType.TRUE))
            return new Expression.Literal(true);
        if (match(TokenType.NIL))
            return new Expression.Literal(null);
        if (match(TokenType.FUN)) {
            FunctionData data = function("lambda");
            return new Expression.FunctionExpr(data.name, data.parameters, data.statements);
        }

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
