package com.craftinginterpreters.jlox.interpreter;

import java.util.Map;

import com.craftinginterpreters.jlox.syntax.Token;

import java.util.HashMap;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment(Environment parent) {
        this.enclosing = parent;
    }

    public Environment() {
        this(null);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "undefined value.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        } 
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "undeclared reference.");
    }

    public void define(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "re definition of already present reference");
        }
        values.put(name.lexeme, value);
    }

    public void declare(Token name) {
        if (values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "re definition of already present reference");
        }
        values.put(name.lexeme, null);
    }
}
