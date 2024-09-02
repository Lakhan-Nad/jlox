package com.craftinginterpreters.jlox.parser;

import com.craftinginterpreters.jlox.syntax.Token;

public class ScopeData {
    public static enum VariableState {
        DECLARED,
        DEFINED,
        ACCESSED
    }

    public final Token name;
    public VariableState state;
    public final int slot;

    ScopeData(Token name, int slot) {
        this.name = name;
        this.slot = slot;
        this.state = VariableState.DECLARED;
    }

    ScopeData(Token name, int slot, VariableState state) {
        this.name = name;
        this.slot = slot;
        this.state = state;
    }
}
