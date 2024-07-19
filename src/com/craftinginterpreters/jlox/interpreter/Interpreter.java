package com.craftinginterpreters.jlox.interpreter;

import com.craftinginterpreters.jlox.syntax.Expression;
import com.craftinginterpreters.jlox.syntax.Expression.Binary;
import com.craftinginterpreters.jlox.syntax.Expression.CommaSeperated;
import com.craftinginterpreters.jlox.syntax.Expression.Grouping;
import com.craftinginterpreters.jlox.syntax.Expression.Literal;
import com.craftinginterpreters.jlox.syntax.Expression.Unary;
import com.craftinginterpreters.jlox.tools.AstPrinter;
import com.craftinginterpreters.jlox.tools.ErrorHandler;
import com.craftinginterpreters.jlox.tools.Logger;

public class Interpreter implements Expression.Visitor<Object> {

    public void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            ErrorHandler.runtimeError(error);
        }
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

    private Object evaluate(Expression expr) {
        Object value = expr.accept(this);
        Logger.trace(String.format("%s evaluated to - %s", new AstPrinter(expr).print(), stringify(value)));
        return value;
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
