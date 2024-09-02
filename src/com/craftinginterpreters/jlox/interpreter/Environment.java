package com.craftinginterpreters.jlox.interpreter;

import com.craftinginterpreters.jlox.syntax.Token;
import java.util.ArrayList;
import java.util.List;

public class Environment {
    final Environment enclosing;

    List<Object> values;

    public Environment(Environment parent) {
        this.enclosing = parent;
        this.values = new ArrayList<>();
    }

    public Environment() {
        this(null);
    }

    public Object get(Token name) {
        Environment container = getContainer(name);
        if (container.values.size() <= name.slot) {
            throw new RuntimeError(name, "trying to access undeclared variable");
        }
        return container.values.get(name.slot);
    }

    public void assign(Token name, Object value) {
        Environment container = getContainer(name);
        if (container.values.size() <= name.slot) {
            throw new RuntimeError(name, "trying to access undeclared variable");
        }
        container.values.add(name.slot, value);
    }

    public void define(Token name, Object value) {
        Environment container = getContainer(name);
        makeContainerValue(name, container);
        container.values.add(name.slot, value); 
    }

    public void declare(Token name) {
        Environment container = getContainer(name);
        makeContainerValue(name, container);
    }

    private void makeContainerValue(Token name, Environment container) {
        while (container.values.size() <= name.slot) {
            container.values.add(null);
        }
    }

    private Environment getContainer(Token name) {
        int hops = name.hops;
        Environment container = this;
        while (hops-- > 0) {
            container = container.enclosing;
        }
        return container;
    }
}
