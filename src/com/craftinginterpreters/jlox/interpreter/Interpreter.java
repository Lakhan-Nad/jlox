package com.craftinginterpreters.jlox.interpreter;

import java.util.ArrayList;
import java.util.List;

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
import com.craftinginterpreters.jlox.syntax.Statement;
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
import com.craftinginterpreters.jlox.tools.AstPrinter;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment environment = new Environment(LoxGlobalEnvironment.Global);

    public void interpret(List<Statement> statements) {
        try {
            for (Statement stmt : statements) {
                execute(stmt);   
            }
        } catch (RuntimeError error) {
            ErrorHandler.runtimeError(error);
        }
    }

    public Object interpret(Expression expression) {
        try {
            return evaluate(expression);
        } catch (RuntimeError error) {
            ErrorHandler.runtimeError(error);
            return null;
        }
    }

    // statements

    @Override
    public Void visitExpr(Expr obj) {
        evaluate(obj.expr);
        return null;
    }

    @Override
    public Void visitPrint(Print obj) {
        Object value = evaluate(obj.expr);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVar(Var obj) {
        environment.declare(obj.name);
        if (obj.initializer != null) {
            Object value = evaluate(obj.initializer);
            environment.assign(obj.name, value);
        }
        return null;
    }

    @Override
    public Void visitBlock(Block obj) {
        executeBlock(obj.stmts, new Environment(this.environment));
        return null;
    }

    @Override
    public Void visitIfElse(IfElse obj) {
        if (isTruthy(evaluate(obj.condition))) {
            execute(obj.thenBranch);
        } else if (obj.elseBranch != null) {
            execute(obj.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhile(While obj) {
        while (isTruthy(evaluate(obj.codition))) {
            try {
                execute(obj.body);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                continue;
            }
        }
        return null;
    }

    @Override
    public Void visitFor(For obj) {
        if (obj.initializer != null) {
            execute(obj.initializer);
        }
        while (isTruthy(evaluate(obj.condition))) {
            try {
                execute(obj.body);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                continue;
            }
            if (obj.change != null) {
                evaluate(obj.change);
            }
        }
        return null;
    }

    @Override
    public Void visitBreak(Break obj) {
        throw new BreakException();
    }

    @Override
    public Void visitContinue(Continue obj) {
        throw new ContinueException();
    }

    @Override
    public Void visitFunction(Function obj) {
        LoxFunction func = new LoxFunction(obj.name, obj.params, obj.stmts, environment);
        environment.define(obj.name, func);
        return null;
    }

    @Override
    public Void visitReturn(Return obj) {
        Object value = null;
        if (obj.expr != null)
            value = evaluate(obj.expr);
        throw new ReturnException(value);
    }

    // expressions
    @Override
    public Object visitFunctionExpr(FunctionExpr obj) {
        return new LoxFunction(obj.name, obj.params, obj.stmts, environment);
    }

    @Override
    public Object visitCall(Call obj) {
        Object callee = evaluate(obj.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expression arg : obj.arguments) {
            arguments.add(evaluate(arg));
        }
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(obj.paren, "can only call functions and classes");
        }
        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(obj.paren,
                    String.format("expected %d arguments but got %d", function.arity(), arguments.size()));
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitLogical(Logical obj) {
        Object left = evaluate(obj.left);
        switch (obj.op.type) {
            case OR:
                if (isTruthy(left)) {
                    return left;
                }
                break;
            case AND:
                if (!isTruthy(left)) {
                    return left;
                }
            default:
                break;
        }
        return evaluate(obj.right);
    }

    @Override
    public Object visitBinary(Binary obj) {
        Object left = evaluate(obj.left);
        Object right = evaluate(obj.right);
        switch (obj.op.type) {
            case STAR:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left * (double) right;
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case STAR_STAR:
                if (left instanceof Double && right instanceof Double) {
                    return Math.pow((double) left, (double) right);
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case MINUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case SLASH:
                if (left instanceof Double && right instanceof Double) {
                    if ((double) right == 0.0) {
                        throw new RuntimeError(obj.op, "Divison by zero is not allowed");
                    }
                    return (double) left / (double) right;
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                if (left instanceof String) {
                    return (String) left + stringify(right);
                }
                if (right instanceof String) {
                    return stringify(left) + (String) right;
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return Utils.compareStrings((String) left, (String) right) > 0;
                }
                return false;
            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return Utils.compareStrings((String) left, (String) right) >= 0;
                }
                return false;
            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return Utils.compareStrings((String) left, (String) right) < 0;
                }
                return false;
            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return Utils.compareStrings((String) left, (String) right) <= 0;
                }
                return false;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                break;
        }
        throw new RuntimeError(obj.op, "Invalid binary operator.");
    }

    @Override
    public Object visitUnary(Unary obj) {
        Object evaluated = evaluate(obj.expr);
        switch (obj.op.type) {
            case MINUS:
                if (evaluated instanceof Double) {
                    return -1 * (double) evaluated;
                }
                throw new RuntimeError(obj.op, "Invalid operand types");
            case BANG:
                return !isTruthy(evaluated);
            default:
                break;
        }
        throw new RuntimeError(obj.op, "Invalid binary operator.");
    }

    @Override
    public Object visitGrouping(Grouping obj) {
        return evaluate(obj.expr);
    }

    @Override
    public Object visitLiteral(Literal obj) {
        return obj.value;
    }

    @Override
    public Object visitCommaSeperated(CommaSeperated obj) {
        Object result = null;
        for (Expression exp : obj.expressions) {
            result = evaluate(exp);
        }
        return result;
    }

    @Override
    public Object visitAssign(Assign obj) {
        Object value = evaluate(obj.value);
        environment.assign(obj.name, value);
        return value;
    }

    @Override
    public Object visitVariable(Variable obj) {
        return environment.get(obj.name);
    }

    // helpers
    private void execute(Statement stmt) {
        Logger.trace(String.format("statement found: %s", new AstPrinter().print(stmt)));
        stmt.accept(this);
    }

    private Object evaluate(Expression expr) {
        Object value = expr.accept(this);
        Logger.trace(String.format("%s evaluated to: %s", new AstPrinter().print(expr), stringify(value)));
        return value;
    }

    void executeBlock(List<Statement> stmts, Environment newEnv) {
        Environment previous = this.environment;
        try {
            this.environment = newEnv;
            for (Statement stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Boolean)
            return (boolean) obj;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null || right == null)
            return false;
        return left.equals(right);
    }

    private String stringify(Object value) {
        if (value == null)
            return "nil";
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }
}
