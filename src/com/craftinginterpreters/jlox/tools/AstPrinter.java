package com.craftinginterpreters.jlox.tools;

import java.util.List;
import java.util.ArrayList;
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

/**
 * AstPrinter
 */
public class AstPrinter implements Expression.Visitor<String>, Statement.Visitor<String> {
    public AstPrinter() {
    }

    public String print(Expression expr) {
        return expr.accept(this);
    }

    public String print(Statement stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitBinary(Binary obj) {
        return "( binary " + obj.left.accept(this) + " " + obj.op.lexeme + " " + obj.right.accept(this) + " )";
    }

    @Override
    public String visitUnary(Unary obj) {
        return "( unary " + obj.op.lexeme + " " + obj.expr.accept(this) + " )";
    }

    @Override
    public String visitGrouping(Grouping obj) {
        return "( group " + obj.expr.accept(this) + ")";
    }

    @Override
    public String visitLiteral(Literal obj) {
        if (obj.value == null)
            return "nil";
        return obj.value.toString();
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
        return "( commaSeperated " + builder.toString() + " )";
    }

    @Override
    public String visitVariable(Variable obj) {
        return "( var " + obj.name.lexeme + " )";
    }

    @Override
    public String visitAssign(Assign obj) {
        return "( assign " + obj.name.lexeme + " = " + obj.value.accept(this) + " )";
    }

    @Override
    public String visitExpr(Expr obj) {
        return "[ expr " + obj.expr.accept(this) + " ]";
    }

    @Override
    public String visitPrint(Print obj) {
        return "[ print " + obj.expr.accept(this) + " ]";
    }

    @Override
    public String visitVar(Var obj) {
        String initlializer = obj.initializer == null ? "" : " = " + obj.initializer.accept(this);
        return "[ var " + obj.name.lexeme + initlializer + " ]";
    }

    @Override
    public String visitBlock(Block obj) {
        List<String> statements = new ArrayList<>();
        for (Statement stmt : obj.stmts) {
            statements.add(stmt.accept(this));
        }
        return "{ block " + String.join("\n ", statements) + " }";
    }

    @Override
    public String visitIfElse(IfElse obj) {
        StringBuilder builder = new StringBuilder();
        builder.append("if ( " + obj.condition.accept(this) + " ) ");
        builder.append(obj.thenBranch.accept(this));
        if (obj.elseBranch != null) {
            builder.append("else ");
            builder.append(obj.elseBranch.accept(this));
        }
        return builder.toString();
    }

    @Override
    public String visitWhile(While obj) {
        return "while (" + obj.codition.accept(this) + " ) " + obj.body.accept(this);
    }

    @Override
    public String visitBreak(Break obj) {
        return "statement break";
    }

    @Override
    public String visitContinue(Continue obj) {
        return "statement continue";
    }

    @Override
    public String visitLogical(Logical obj) {
        return "[ logical " + obj.left.accept(this) + " " + obj.op.lexeme + " " + obj.right.accept(this) + " ]";
    }

    @Override
    public String visitFor(For obj) {
        return "for";
    }

    @Override
    public String visitFunction(Function obj) {
        return "function";
    }

    @Override
    public String visitReturn(Return obj) {
        return "return";
    }

    @Override
    public String visitCall(Call obj) {
        return "call";
    }

    @Override
    public String visitFunctionExpr(FunctionExpr obj) {
        return "functionExpression";
    }

}