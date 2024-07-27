package com.craftinginterpreters.jlox.interpreter;

import java.util.List;

import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.TokenType;

public class LoxGlobalEnvironment {
    static Environment Global = new Environment();
    static {
        Global.define(getGlobalToken("clock"), new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis()/1000.0;
            }

            @Override
            public int arity() {
               return 0;
            }
        });
    }

    static Token getGlobalToken(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, -1);
    }
}