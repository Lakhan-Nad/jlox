package com.craftinginterpreters.jlox.interpreter;

import java.util.Map;

import com.craftinginterpreters.jlox.syntax.Token;

import java.util.HashMap;

public class Environment {
    final Environment enclosing;

    private class ValueWrapper {
        public boolean assigned;
        public Object value;

        ValueWrapper(Object value) {
            this.assigned = true;
            this.value = value;
        }

        ValueWrapper() {
            this.assigned = false;
            this.value = null;
        }
    }

    private final Map<String, ValueWrapper> values = new HashMap<>();

    public Environment(Environment parent) {
        this.enclosing = parent;
    }

    public Environment() {
        this(null);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            ValueWrapper val = values.get(name.lexeme);
            if (!val.assigned) {
                throw new RuntimeError(name, "variable is not assigned yet.");
            }
            return val.value;
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "undefined value.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            ValueWrapper val = values.get(name.lexeme);
            val.assigned = true;
            val.value = value;
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
        values.put(name.lexeme, new ValueWrapper(value));
    }

    public void declare(Token name) {
        if (values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "re definition of already present reference");
        }
        values.put(name.lexeme, new ValueWrapper());
    }
}
