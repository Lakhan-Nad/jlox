package com.craftinginterpreters.jlox.interpreter;

import java.util.List;

import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Expression.Assign;
import com.craftinginterpreters.jlox.syntax.Expression.Binary;
import com.craftinginterpreters.jlox.syntax.Expression.CommaSeperated;
import com.craftinginterpreters.jlox.syntax.Expression.Grouping;
import com.craftinginterpreters.jlox.syntax.Expression.Literal;
import com.craftinginterpreters.jlox.syntax.Expression.Unary;
import com.craftinginterpreters.jlox.syntax.Expression.Variable;
import com.craftinginterpreters.jlox.syntax.Statement;
import com.craftinginterpreters.jlox.syntax.Statement.Block;
import com.craftinginterpreters.jlox.syntax.Statement.Expr;
import com.craftinginterpreters.jlox.syntax.Statement.Print;
import com.craftinginterpreters.jlox.syntax.Statement.Var;
import com.craftinginterpreters.jlox.tools.AstPrinter;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment environment = new Environment();

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
       if (obj.initializer != null) {
            environment.declare(obj.name);
            Object value = evaluate(obj.initializer);
            environment.assign(obj.name, value);
       } else {
            environment.declare(obj.name);
       }
       return null;
    }

    @Override
    public Void visitBlock(Block obj) {
        executeBlock(obj.stmts, new Environment(environment));
        return null;
    }

    // expressions
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

    private void executeBlock(List<Statement> stmts, Environment newEnv) {
        Environment previous = newEnv.enclosing;
        try {
            this.environment = newEnv;
            for (Statement stmt: stmts) {
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
