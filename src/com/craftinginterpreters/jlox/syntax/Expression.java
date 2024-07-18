package com.craftinginterpreters.jlox.syntax;

public abstract class Expression {
	abstract <R> R accept(Visitor<R> visitor);

	public static class Binary extends Expression {
		public Binary(Expression left, Token op, Expression right) {
			this.left = left;
			this.op = op;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinary(this);
		}

		final Expression left;
		final Token op;
		final Expression right;
	}

	public static class Unary extends Expression {
		public Unary(Token op, Expression expr) {
			this.op = op;
			this.expr = expr;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnary(this);
		}

		final Token op;
		final Expression expr;
	}

	public static class Grouping extends Expression {
		public Grouping(Expression expr) {
			this.expr = expr;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGrouping(this);
		}

		final Expression expr;
	}

	public static class Literal extends Expression {
		public Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteral(this);
		}

		final Object value;
	}
}


interface Visitor<T> {

	T visitBinary(Expression.Binary obj);

	T visitUnary(Expression.Unary obj);

	T visitGrouping(Expression.Grouping obj);

	T visitLiteral(Expression.Literal obj);
}
