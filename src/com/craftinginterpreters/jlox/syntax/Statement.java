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

	public static class IfElse extends Statement {
		public IfElse(Expression condition, Statement thenBranch, Statement elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfElse(this);
		}

		public final Expression condition;
		public final Statement thenBranch;
		public final Statement elseBranch;
	}

	public static class While extends Statement {
		public While(Expression codition, Statement body) {
			this.codition = codition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhile(this);
		}

		public final Expression codition;
		public final Statement body;
	}

	public static class For extends Statement {
		public For(Statement initializer, Expression condition, Statement body, Expression change) {
			this.initializer = initializer;
			this.condition = condition;
			this.body = body;
			this.change = change;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFor(this);
		}

		public final Statement initializer;
		public final Expression condition;
		public final Statement body;
		public final Expression change;
	}

	public static class Function extends Statement {
		public Function(Token name, List<Token> params, List<Statement> stmts) {
			this.name = name;
			this.params = params;
			this.stmts = stmts;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunction(this);
		}

		public final Token name;
		public final List<Token> params;
		public final List<Statement> stmts;
	}

	public static class Break extends Statement {
		public Break() {
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBreak(this);
		}

	}

	public static class Continue extends Statement {
		public Continue() {
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitContinue(this);
		}

	}

	public static class Return extends Statement {
		public Return(Token keyword, Expression expr) {
			this.keyword = keyword;
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturn(this);
		}

		public final Token keyword;
		public final Expression expr;
	}
	public interface Visitor<T> {

		T visitBlock(Statement.Block obj);

		T visitExpr(Statement.Expr obj);

		T visitPrint(Statement.Print obj);

		T visitVar(Statement.Var obj);

		T visitIfElse(Statement.IfElse obj);

		T visitWhile(Statement.While obj);

		T visitFor(Statement.For obj);

		T visitFunction(Statement.Function obj);

		T visitBreak(Statement.Break obj);

		T visitContinue(Statement.Continue obj);

		T visitReturn(Statement.Return obj);
	}
}


