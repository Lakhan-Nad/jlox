package com.craftinginterpreters.jlox.interpreter;

import java.util.List;

import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;

public class FunctionData {
    public final List<Statement> statements;
    public final List<Token> parameters;
    public final Token name;

    public FunctionData(
            Token name,
            List<Token> parameters,
            List<Statement> statements) {
        this.name = name;
        this.parameters = parameters;
        this.statements = statements;
    }
}
