package com.craftinginterpreters.jlox.syntax;

import java.util.List;

public abstract class Statement {
	public abstract <R> R accept(Visitor<R> visitor);

	public static class Block extends Statement {
		public Block(List<Statement> stmts) {
			this.stmts = stmts;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlock(this);
		}

		public final List<Statement> stmts;
	}

	public static class Expr extends Statement {
		public Expr(Expression expr) {
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpr(this);
		}

		public final Expression expr;
	}

	public static class Print extends Statement {
		public Print(Expression expr) {
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitPrint(this);
		}

		public final Expression expr;
	}

	public static class Var extends Statement {
		public Var(Token name, Expression initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVar(this);
		}

		public final Token name;
		public final Expression initializer;
	}
	public interface Visitor<T> {

		T visitBlock(Statement.Block obj);

		T visitExpr(Statement.Expr obj);

		T visitPrint(Statement.Print obj);

		T visitVar(Statement.Var obj);
	}
}


