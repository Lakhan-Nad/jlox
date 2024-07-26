package com.craftinginterpreters.jlox.syntax;

import java.util.List;

public abstract class Expression {
	public abstract <R> R accept(Visitor<R> visitor);

	public static class Assign extends Expression {
		public Assign(Token name, Expression value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssign(this);
		}

		public final Token name;
		public final Expression value;
	}

	public static class Binary extends Expression {
		public Binary(Expression left, Token op, Expression right) {
			this.left = left;
			this.op = op;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinary(this);
		}

		public final Expression left;
		public final Token op;
		public final Expression right;
	}

	public static class Unary extends Expression {
		public Unary(Token op, Expression expr) {
			this.op = op;
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnary(this);
		}

		public final Token op;
		public final Expression expr;
	}

	public static class Grouping extends Expression {
		public Grouping(Expression expr) {
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGrouping(this);
		}

		public final Expression expr;
	}

	public static class Literal extends Expression {
		public Literal(Object value) {
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteral(this);
		}

		public final Object value;
	}

	public static class CommaSeperated extends Expression {
		public CommaSeperated(List<Expression> expressions) {
			this.expressions = expressions;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCommaSeperated(this);
		}

		public final List<Expression> expressions;
	}

	public static class Variable extends Expression {
		public Variable(Token name) {
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariable(this);
		}

		public final Token name;
	}

	public static class Logical extends Expression {
		public Logical(Expression left, Token op, Expression right) {
			this.left = left;
			this.op = op;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLogical(this);
		}

		public final Expression left;
		public final Token op;
		public final Expression right;
	}
	public interface Visitor<T> {

		T visitAssign(Expression.Assign obj);

		T visitBinary(Expression.Binary obj);

		T visitUnary(Expression.Unary obj);

		T visitGrouping(Expression.Grouping obj);

		T visitLiteral(Expression.Literal obj);

		T visitCommaSeperated(Expression.CommaSeperated obj);

		T visitVariable(Expression.Variable obj);

		T visitLogical(Expression.Logical obj);
	}
}


