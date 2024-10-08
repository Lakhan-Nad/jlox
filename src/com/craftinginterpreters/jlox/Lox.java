package com.craftinginterpreters.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.craftinginterpreters.jlox.scanner.Scanner;
import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.interpreter.Interpreter;
import com.craftinginterpreters.jlox.parser.Parser;
import com.craftinginterpreters.jlox.parser.Resolver;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;
import com.craftinginterpreters.jlox.tools.Logger.Level;

public class Lox {
  public static Interpreter interpreter = new Interpreter();
  
  static {
    Logger.setLogLevel(Level.INFO);
  }

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    // Indicate an error in the exit code.
    if (ErrorHandler.hadError)
      System.exit(65);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);
      ErrorHandler.resetErrors();
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    if (ErrorHandler.hadError) {
      return;
    }

    Parser parser = new Parser(tokens);
    List<Statement> statements = parser.parse();

    if (ErrorHandler.hadError) {
      return;
    }

    Resolver resolver = new Resolver();
    resolver.resolve(statements);

    if (ErrorHandler.hadError) {
      return;
    }

    interpreter.interpret(statements);
  }
}