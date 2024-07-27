package com.craftinginterpreters.jlox.interpreter;

import java.util.List;

import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;

public class LoxFunction implements LoxCallable {
    private final List<Statement> statements;
    private final List<Token> parameters;
    private final Token name;
    private final Environment enclosing;
    private final String nameStr;

    LoxFunction(
        Token name,
        List<Token> parameters,
        List<Statement> statements, 
        Environment enclosing
    ) {
        this.name = name;
        if (this.name != null) {
            this.nameStr = this.name.lexeme;
        } else {
            this.nameStr = "<anonymous>";
        }
        this.parameters = parameters;
        this.statements = statements;
        this.enclosing = enclosing;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(this.enclosing);
        for (int i = 0; i < arguments.size(); i++) {
            env.define(this.parameters.get(i), arguments.get(i));
        }
        try {
            interpreter.executeBlock(this.statements, env);
        } catch(ReturnException exp) {
            return exp.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return this.parameters.size();
    }
    
    @Override
    public String toString() {
        return String.format("<fn %s>", this.nameStr);
    }
}
