package com.craftinginterpreters.jlox.syntax;

import com.craftinginterpreters.jlox.syntax.Expression.Binary;
import com.craftinginterpreters.jlox.syntax.Expression.CommaSeperated;
import com.craftinginterpreters.jlox.syntax.Expression.Grouping;
import com.craftinginterpreters.jlox.syntax.Expression.Literal;
import com.craftinginterpreters.jlox.syntax.Expression.Unary;
import com.craftinginterpreters.jlox.syntax.Expression.Visitor;

/**
 * AstPrinter
 */
public class AstPrinter implements Visitor<String> {

    final Expression expr;

    public AstPrinter(Expression expr) {
        this.expr = expr;
    }

    @Override
    public String visitBinary(Binary obj) {
        return obj.left.accept(this) + " " + obj.op.lexeme + " " + obj.right.accept(this);
    }

    @Override
    public String visitUnary(Unary obj) {
        return obj.op.lexeme + " " + obj.expr.accept(this);
    }

    @Override
    public String visitGrouping(Grouping obj) {
        return "(" + obj.expr.accept(this) + ")";
    }

    @Override
    public String visitLiteral(Literal obj) {
        if (obj.value == null)
            return "nil";
        return obj.value.toString();
    }

    public String print() {
        return this.expr.accept(this);
    }

    @Override
    public String visitCommaSeperated(CommaSeperated obj) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < obj.expressions.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(obj.expressions.get(i).accept(this));
        }
        return builder.toString();
    }

}