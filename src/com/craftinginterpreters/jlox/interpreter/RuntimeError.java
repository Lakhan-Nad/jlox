package com.craftinginterpreters.jlox.interpreter;

import com.craftinginterpreters.jlox.syntax.Token;

public class RuntimeError extends RuntimeException {
  public final Token token;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}