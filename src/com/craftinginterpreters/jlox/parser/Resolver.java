package com.craftinginterpreters.jlox.parser;

import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Token;
import com.craftinginterpreters.jlox.syntax.Statement.Block;
import com.craftinginterpreters.jlox.syntax.Statement.Break;
import com.craftinginterpreters.jlox.syntax.Statement.Continue;
import com.craftinginterpreters.jlox.syntax.Statement.Expr;
import com.craftinginterpreters.jlox.syntax.Statement.For;
import com.craftinginterpreters.jlox.syntax.Statement.Function;
import com.craftinginterpreters.jlox.syntax.Statement.IfElse;
import com.craftinginterpreters.jlox.syntax.Statement.Print;
import com.craftinginterpreters.jlox.syntax.Statement.Return;
import com.craftinginterpreters.jlox.syntax.Statement.Var;
import com.craftinginterpreters.jlox.syntax.Statement.While;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;
import com.craftinginterpreters.jlox.interpreter.RuntimeError;
import com.craftinginterpreters.jlox.parser.ScopeData.VariableState;
import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Expression.Assign;
import com.craftinginterpreters.jlox.syntax.Expression.Binary;
import com.craftinginterpreters.jlox.syntax.Expression.Call;
import com.craftinginterpreters.jlox.syntax.Expression.CommaSeperated;
import com.craftinginterpreters.jlox.syntax.Expression.FunctionExpr;
import com.craftinginterpreters.jlox.syntax.Expression.Grouping;
import com.craftinginterpreters.jlox.syntax.Expression.Literal;
import com.craftinginterpreters.jlox.syntax.Expression.Logical;
import com.craftinginterpreters.jlox.syntax.Expression.Unary;
import com.craftinginterpreters.jlox.syntax.Expression.Variable;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Stack<Map<String, ScopeData>> scopes;

    public Resolver() {
        this.scopes = new Stack<>();
    }

    public void resolve(List<Statement> statements) {
        try {
            for (Statement stmt : statements) {
                resolve(stmt);   
            }
        } catch (RuntimeError error) {
            ErrorHandler.runtimeError(error);
        }
    }

    @Override
    public Void visitBlock(Block obj) {
        beginScope();
        for (Statement stmt: obj.stmts) {
            resolve(stmt);
        }   
        endScope();
        return null;
    }

    @Override
    public Void visitExpr(Expr obj) {
        resolve(obj.expr);
        return null;
    }

    @Override
    public Void visitPrint(Print obj) {
        resolve(obj.expr);
        return null;
    }

    @Override
    public Void visitVar(Var obj) {
        declare(obj.name);
        if (obj.initializer != null) {
            resolve(obj.initializer);
            define(obj.name);
        }
        return null;   
    }

    @Override
    public Void visitIfElse(IfElse obj) {
        resolve(obj.condition);
        resolve(obj.thenBranch);
        if (obj.elseBranch != null) {
            resolve(obj.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhile(While obj) {
        if (obj.codition != null) {
            resolve(obj.codition);
        }
        resolve(obj.body);
        return null;
    }

    @Override
    public Void visitFor(For obj) {
        if (obj.initializer != null) {
            resolve(obj.initializer);
        }
        if (obj.condition != null) {
            resolve(obj.condition);
        }
        if (obj.change != null) {
            resolve(obj.change);
        }
        resolve(obj.body);
        return null;
    }

    @Override
    public Void visitFunction(Function obj) {
        declare(obj.name);
        define(obj.name);
        beginScope();
        for (Token param: obj.params) {
            declare(param);
            define(param);
        }
        for (Statement stmt: obj.stmts) {
            resolve(stmt);
        }
        endScope();
        return null;
    }

    @Override
    public Void visitBreak(Break obj) {
        return null;
    }

    @Override
    public Void visitContinue(Continue obj) {
        return null;
    }

    @Override
    public Void visitReturn(Return obj) {
        if (obj.expr != null) {
            resolve(obj.expr);
        }
        return null;
    }

    // expressions

    @Override
    public Void visitAssign(Assign obj) {
        resolve(obj.value);
        assign(obj, obj.name);
        return null;
    }

    @Override
    public Void visitBinary(Binary obj) {
        resolve(obj.left);
        resolve(obj.right);
        return null;
    }

    @Override
    public Void visitUnary(Unary obj) {
        resolve(obj.expr);
        return null;
    }

    @Override
    public Void visitGrouping(Grouping obj) {
        resolve(obj.expr);
        return null;
    }

    @Override
    public Void visitLiteral(Literal obj) {
        return null;
    }

    @Override
    public Void visitCommaSeperated(CommaSeperated obj) {
        for (Expression expr: obj.expressions) {
            resolve(expr);
        }
        return null;
    }

    @Override
    public Void visitVariable(Variable obj) {
        access(obj, obj.name);
        return null;
    }

    @Override
    public Void visitLogical(Logical obj) {
        resolve(obj.left);
        resolve(obj.right);
        return null;
    }

    @Override
    public Void visitCall(Call obj) {
        resolve(obj.callee);
        for (Expression expr: obj.arguments) {
            resolve(expr);
        }
        return null;
    }

    @Override
    public Void visitFunctionExpr(FunctionExpr obj) {
        if (obj.name != null) {
            declare(obj.name);
            define(obj.name);
        }
        for (Token param: obj.params) {
            declare(param);
            define(param);
        }
        for (Statement stmt: obj.stmts) {
            resolve(stmt);
        }
        return null;
    }

    // helpers
    private void resolve(Statement stmt) {
        stmt.accept(this);
    }

    private void resolve(Expression expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        if (scopes.isEmpty()) return;
        Map<String, ScopeData> scope = scopes.peek();
        scopes.pop();
        for (Map.Entry<String, ScopeData> entry: scope.entrySet()) {
            ScopeData value = entry.getValue();
            if (value.state != ScopeData.VariableState.ACCESSED) {
                ErrorHandler.parseError(new ParseError(value.name, "variable declared but not accessed"));
            }
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty())  return;
        Map<String, ScopeData> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            ErrorHandler.parseError(new ParseError(name, "variable with same name already declared in this scope"));
        }
        scope.put(name.lexeme, new ScopeData(name, scope.size()));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, ScopeData> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            scope.get(name.lexeme).state = ScopeData.VariableState.DEFINED;
        } else {
            scope.put(name.lexeme, new ScopeData(name, scope.size(), VariableState.DEFINED));
        }
    }

    private void access(
        Expression expr, 
        Token name    
    ) {
        Logger.trace(String.format("inside resolve for %s with scopes %d", name.lexeme, scopes.size() - 1));
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, ScopeData> scope = scopes.get(i);
            if (scope.containsKey(name.lexeme)) {
                Logger.trace(String.format("inside resolve for %s trying %d out of %d", name.lexeme, i, scopes.size() - 1));
                ScopeData data = scope.get(name.lexeme);
                if (data.state == ScopeData.VariableState.DECLARED) {
                    ErrorHandler.parseError(new ParseError(name, "accessing without being defined"));
                } else {
                    data.state = ScopeData.VariableState.ACCESSED;
                    resolveUsage(name, scopes.size() - i - 1, data.slot);
                }
                return;
            }
        }
        ErrorHandler.parseError(new ParseError(name, "trying to access an undeclared variable"));
    }

    private void assign(
        Expression expr,
        Token name
    ) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, ScopeData> scope = scopes.get(i);
            if (scope.containsKey(name.lexeme)) {
                ScopeData data = scope.get(name.lexeme);
                data.state = ScopeData.VariableState.ACCESSED;
                resolveUsage(name, scopes.size() - i - 1, data.slot);
                return;
            }
        }
        ErrorHandler.parseError(new ParseError(name, "trying to assign an undeclared variable"));
    }

    private void resolveUsage(Token name, int hops, int slot) {
        name.attachEnvData(hops, slot);
    }
}